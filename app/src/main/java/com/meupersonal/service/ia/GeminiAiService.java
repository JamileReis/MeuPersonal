package com.meupersonal.service.ia;


import com.meupersonal.BuildConfig;
import com.meupersonal.model.questionario.Questionnaire;
import com.meupersonal.model.treino.Exercise;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiAiService implements AiService {
    public static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-2.5-flash:generateContent";
    private static final String SYSTEM_PROMPT =
            "Você é o assistente de treino do MeuPersonal. "
                    + "Responda sempre em português do Brasil. "
                    + "Não invente dados pessoais, objetivos, lesões "
                    + "ou limitações. Priorize a segurança dos exercícios.";

    private static final int TEMPO_CONEXAO_MS = 20_000;
    private static final int TEMPO_RESPOSTA_MS = 90_000;
    private static final int MAX_TOKENS = 8192;

    private final String chaveApi;

    public GeminiAiService(String chaveApi) {
        if (chaveApi == null || chaveApi.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "A chave da API do Gemini não foi configurada."
            );
        }

        this.chaveApi = chaveApi.trim();
    }

    @Override
    public String generateTrainingPlan(Questionnaire questionnaire)
            throws IOException {

        if (questionnaire == null) {
            throw new IllegalArgumentException(
                    "O questionário não foi informado."
            );
        }

        if (questionnaire.getUserId() == null) {
            throw new IllegalArgumentException(
                    "O questionário não está associado a um usuário."
            );
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "Prepare uma proposta de treino para o MeuPersonal.\n\n"
        );

        prompt.append("Dados disponíveis:\n");

        adicionarLinha(
                prompt,
                "ID do questionário",
                questionnaire.getId()
        );

        adicionarLinha(
                prompt,
                "ID do usuário",
                questionnaire.getUserId()
        );

        adicionarLinha(
                prompt,
                "Questionário completo",
                questionnaire.getCompletado()
        );

        prompt.append(
                "\nOs objetivos, preferências e dados de saúde "
                        + "não estão necessariamente disponíveis neste objeto. "
                        + "Não invente essas informações. "
                        + "Indique os dados necessários que estiverem faltando.\n"
        );

        prompt.append(
                "\nRetorne somente JSON válido neste formato: "
                        + "{\"titulo\":\"\","
                        + "\"observacoes\":\"\","
                        + "\"dadosFaltantes\":[],"
                        + "\"dias\":[]}"
        );

        return executar(prompt.toString(), true, 0.4);
    }

    @Override
    public String generateExerciseDescription(Exercise exercise)
            throws IOException {

        if (exercise == null) {
            throw new IllegalArgumentException(
                    "O exercício não foi informado."
            );
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "Explique o exercício a seguir em português do Brasil.\n"
                        + "Inclua posição inicial, execução passo a passo, "
                        + "músculos trabalhados, erros comuns "
                        + "e cuidados de segurança.\n\n"
        );

        adicionarDadosExercicio(prompt, exercise);

        return executar(prompt.toString(), false, 0.6);
    }

    @Override
    public String generateExerciseImage(Exercise exercise)
            throws IOException {

        if (exercise == null) {
            throw new IllegalArgumentException(
                    "O exercício não foi informado."
            );
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "Crie uma descrição visual detalhada para ilustrar "
                        + "a execução correta deste exercício. "
                        + "Descreva a posição do corpo, o equipamento, "
                        + "o ângulo da imagem e as etapas do movimento. "
                        + "Não afirme que um arquivo de imagem foi gerado.\n\n"
        );

        adicionarDadosExercicio(prompt, exercise);

        return executar(prompt.toString(), false, 0.6);
    }

    @Override
    public String sendPrompt(String prompt) throws IOException {

        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O prompt não pode estar vazio."
            );
        }

        return executar(prompt, false, 0.7);
    }

    private String executar(
            String prompt,
            boolean respostaJson,
            double temperatura
    ) throws IOException {

        HttpURLConnection conexao = null;

        try {
            JSONObject configuracao = new JSONObject()
                    .put("temperature", temperatura)
                    .put("maxOutputTokens", MAX_TOKENS);

            if (respostaJson) {
                configuracao.put(
                        "responseMimeType",
                        "application/json"
                );
            }

            JSONObject instrucaoSistema = new JSONObject()
                    .put(
                            "parts",
                            new JSONArray().put(
                                    new JSONObject().put(
                                            "text",
                                            SYSTEM_PROMPT
                                    )
                            )
                    );

            JSONObject conteudoUsuario = new JSONObject()
                    .put(
                            "parts",
                            new JSONArray().put(
                                    new JSONObject().put(
                                            "text",
                                            prompt
                                    )
                            )
                    );

            JSONObject corpoRequisicao = new JSONObject()
                    .put("systemInstruction", instrucaoSistema)
                    .put(
                            "contents",
                            new JSONArray().put(conteudoUsuario)
                    )
                    .put("generationConfig", configuracao);

            URL url = new URL(API_URL);

            conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestMethod("POST");
            conexao.setConnectTimeout(TEMPO_CONEXAO_MS);
            conexao.setReadTimeout(TEMPO_RESPOSTA_MS);
            conexao.setDoOutput(true);

            conexao.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=utf-8"
            );

            conexao.setRequestProperty(
                    "x-goog-api-key",
                    chaveApi
            );

            byte[] dados = corpoRequisicao.toString()
                    .getBytes(StandardCharsets.UTF_8);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(dados);
            }

            int codigoResposta = conexao.getResponseCode();

            InputStream fluxoResposta;

            if (codigoResposta >= 200 && codigoResposta < 300) {
                fluxoResposta = conexao.getInputStream();
            } else {
                fluxoResposta = conexao.getErrorStream();
            }

            String corpoResposta = lerResposta(fluxoResposta);

            if (codigoResposta < 200 || codigoResposta >= 300) {
                throw new IOException(
                        "Erro na API Gemini (HTTP "
                                + codigoResposta
                                + "): "
                                + extrairMensagemErro(corpoResposta)
                );
            }

            return extrairTextoResposta(corpoResposta);

        } catch (JSONException e) {
            throw new IOException(
                    "Erro ao montar ou interpretar o JSON da API.",
                    e
            );
        } finally {
            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }

    private String lerResposta(InputStream fluxo) throws IOException {

        if (fluxo == null) {
            return "";
        }

        try (InputStream entrada = fluxo;
             ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int quantidade;

            while ((quantidade = entrada.read(buffer)) != -1) {
                saida.write(buffer, 0, quantidade);
            }

            return saida.toString(StandardCharsets.UTF_8.name());
        }
    }

    private String extrairTextoResposta(String corpoResposta)
            throws IOException, JSONException {

        JSONObject resposta = new JSONObject(corpoResposta);

        JSONArray candidatos = resposta.optJSONArray("candidates");

        if (candidatos == null || candidatos.length() == 0) {
            JSONObject informacao = resposta.optJSONObject("promptFeedback");

            String motivo = informacao != null
                    ? informacao.optString(
                    "blockReason",
                    "A API não retornou candidatos."
            )
                    : "A API não retornou candidatos.";

            throw new IOException(
                    "Não foi possível obter uma resposta do Gemini: "
                            + motivo
            );
        }

        JSONObject candidato = candidatos.optJSONObject(0);

        if (candidato == null) {
            throw new IOException(
                    "A resposta da API contém um candidato inválido."
            );
        }

        JSONObject conteudo = candidato.optJSONObject("content");

        if (conteudo == null) {
            throw new IOException(
                    "A resposta do Gemini não contém conteúdo."
            );
        }

        JSONArray partes = conteudo.optJSONArray("parts");

        if (partes == null || partes.length() == 0) {
            throw new IOException(
                    "A resposta do Gemini não contém texto."
            );
        }

        StringBuilder texto = new StringBuilder();

        for (int indice = 0; indice < partes.length(); indice++) {
            JSONObject parte = partes.optJSONObject(indice);

            if (parte != null) {
                String trecho = parte.optString("text", "");

                if (!trecho.isEmpty()) {
                    if (texto.length() > 0) {
                        texto.append('\n');
                    }

                    texto.append(trecho);
                }
            }
        }

        if (texto.length() == 0) {
            throw new IOException(
                    "O Gemini retornou uma resposta sem texto."
            );
        }

        return texto.toString();
    }

    private String extrairMensagemErro(String corpoResposta) {

        if (corpoResposta == null || corpoResposta.trim().isEmpty()) {
            return "A API não forneceu detalhes sobre o erro.";
        }

        try {
            JSONObject resposta = new JSONObject(corpoResposta);
            JSONObject erro = resposta.optJSONObject("error");

            if (erro != null) {
                return erro.optString("message", corpoResposta);
            }

        } catch (JSONException e) {
            // A resposta não era um JSON de erro válido.
        }

        return corpoResposta;
    }

    private void adicionarDadosExercicio(
            StringBuilder prompt,
            Exercise exercise
    ) {
        adicionarLinha(prompt, "Nome", exercise.getNome());
        adicionarLinha(prompt, "Descrição", exercise.getDescricao());
        adicionarLinha(
                prompt,
                "Grupo muscular",
                exercise.getMisturaGrupo()
        );
        adicionarLinha(
                prompt,
                "Instruções existentes",
                exercise.getInstrucao()
        );
        adicionarLinha(prompt, "Séries", exercise.getSeries());
        adicionarLinha(prompt, "Repetições", exercise.getRepeticoes());
        adicionarLinha(
                prompt,
                "Descanso em segundos",
                exercise.getSegundosDescanso()
        );
        adicionarLinha(prompt, "Intensidade", exercise.getIntensidade());
        adicionarLinha(prompt, "Carga", exercise.getCarga());
    }

    private void adicionarLinha(
            StringBuilder texto,
            String rotulo,
            Object valor
    ) {
        if (valor != null) {
            texto.append(rotulo)
                    .append(": ")
                    .append(valor)
                    .append('\n');
        }
    }
}