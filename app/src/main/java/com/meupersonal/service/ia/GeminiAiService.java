package com.meupersonal.service.ia;
import android.util.Base64;

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
    private static final String BASE =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final String API_URL = BASE + "gemini-3.8-flash:generateContent";

    private static final String[] FALLBACK_URLS = {
            BASE + "gemini-3.7-flash:generateContent",
            BASE + "gemini-3.5-flash:generateContent",
            BASE + "gemini-3.5-flash-lite:generateContent"
    };

    private static final String IMAGE_API_URL = BASE + "gemini-2.5-flash-image:generateContent";

    private static final int MAX_TENTATIVAS = 3;
    private static final String SYSTEM_PROMPT =
            "Você é o assistente de treino do MeuPersonal. "
                    + "Responda sempre em português do Brasil. "
                    + "Não invente dados pessoais, objetivos, lesões "
                    + "ou limitações. Priorize a segurança dos exercícios.";

    private static final int TEMPO_CONEXAO_MS = 20_000;
    private static final int TEMPO_RESPOSTA_MS = 90_000;
    private static final int MAX_TOKENS = 32768;

    private final String chaveApi;

    public GeminiAiService() {
        this(API_KEY);
    }
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

        prompt.append("Prepare uma proposta geral e segura de treino para o MeuPersonal.\n\n");

        prompt.append("Dados disponíveis:\n");

        adicionarLinha(
                prompt,
                "Questionário completo",
                questionnaire.getCompletado()
        );

        prompt.append("\nEste objeto não contém objetivo, histórico médico, equipamentos ou preferências. "
                + "Não presuma esses dados e não faça diagnóstico. Como plano padrão, use objetivo SAUDE_GERAL, "
                + "três sessões curtas de corpo inteiro com movimentos básicos e opções sem equipamento. "
                + "Deixe explícito em observacoes que o plano é geral e precisa ser ajustado ao perfil por profissional.\n");
        prompt.append("Retorne somente JSON válido com esta estrutura: "
                + "{\"titulo\":\"\",\"objetivo\":\"SAUDE_GERAL\",\"observacoes\":\"\","
                + "\"dias\":[{\"dia\":\"Segunda-feira\",\"nome\":\"\",\"duracaoMinutos\":30,"
                + "\"exercicios\":[{\"nome\":\"\",\"grupoMuscular\":\"\",\"series\":2,"
                + "\"repeticoes\":\"8-12\",\"descansoSegundos\":60,\"instrucao\":\"\"}]}]}. "
                + "Gere exatamente três dias, com exercícios e duração positivos, sem campo dadosFaltantes.");

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

    public byte[] generateWorkoutImage(String workoutName, String exerciseNames)
            throws IOException {
        if (workoutName == null || workoutName.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do treino não foi informado.");
        }
        String prompt = "Crie uma única ilustração educativa, clara e realista, "
                + "em formato horizontal, que demonstre os exercícios deste treino. "
                + "Mostre pessoas adultas com postura correta e equipamento adequado. "
                + "Sem texto, letras, logotipos, marcas d'água ou informações médicas. "
                + "Nome do treino: " + workoutName + ". Exercícios: "
                + (exerciseNames == null ? "" : exerciseNames);
        try {
            JSONObject body = new JSONObject()
                    .put("contents", new JSONArray().put(new JSONObject()
                            .put("parts", new JSONArray().put(new JSONObject().put("text", prompt)))))
                    .put("generationConfig", new JSONObject()
                            .put("responseModalities", new JSONArray().put("TEXT").put("IMAGE")));
            JSONObject response = new JSONObject(postJson(IMAGE_API_URL, body));
            JSONArray candidates = response.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject candidate = candidates.optJSONObject(0);
                JSONObject content = candidate == null ? null : candidate.optJSONObject("content");
                JSONArray parts = content == null ? null : content.optJSONArray("parts");
                if (parts != null) {
                    for (int i = 0; i < parts.length(); i++) {
                        JSONObject inline = parts.optJSONObject(i);
                        JSONObject data = inline == null ? null : inline.optJSONObject("inlineData");
                        if (data == null && inline != null) data = inline.optJSONObject("inline_data");
                        if (data != null) {
                            String encoded = data.optString("data", "");
                            if (!encoded.isEmpty()) {
                                try { return Base64.decode(encoded, Base64.DEFAULT); }
                                catch (IllegalArgumentException e) {
                                    throw new IOException("A imagem retornada pelo Gemini está inválida.", e);
                                }
                            }
                        }
                    }
                }
            }
            throw new IOException("O Gemini respondeu, mas não retornou dados de imagem. Tente novamente.");
        } catch (JSONException e) {
            throw new IOException("Não foi possível interpretar a imagem retornada pelo Gemini.", e);
        }
    }

    private JSONObject montarCorpo(String prompt, boolean respostaJson,
                                   double temperatura, boolean comThinking)
            throws JSONException {
        JSONObject configuracao = new JSONObject()
                .put("temperature", temperatura)
                .put("maxOutputTokens", MAX_TOKENS);
        if (respostaJson) configuracao.put("responseMimeType", "application/json");
        if (comThinking) {
            configuracao.put("thinkingConfig", new JSONObject().put("thinkingLevel", "low"));
        }
        JSONObject instrucaoSistema = new JSONObject().put("parts", new JSONArray()
                .put(new JSONObject().put("text", SYSTEM_PROMPT)));
        JSONObject conteudoUsuario = new JSONObject().put("parts", new JSONArray()
                .put(new JSONObject().put("text", prompt)));
        return new JSONObject()
                .put("systemInstruction", instrucaoSistema)
                .put("contents", new JSONArray().put(conteudoUsuario))
                .put("generationConfig", configuracao);
    }

    private String executar(String prompt, boolean respostaJson, double temperatura)
            throws IOException {
        try {
            String[] urls = new String[FALLBACK_URLS.length + 1];
            urls[0] = API_URL;
            System.arraycopy(FALLBACK_URLS, 0, urls, 1, FALLBACK_URLS.length);

            String resposta = null;
            IOException ultimoErro = null;

            for (int i = 0; i < urls.length; i++) {
                JSONObject body = montarCorpo(prompt, respostaJson, temperatura, i == 0);
                try {
                    resposta = postJson(urls[i], body);
                    break;
                } catch (IOException e) {
                    ultimoErro = e;
                    String m = e.getMessage() == null ? "" : e.getMessage();
                    boolean tentarProximo = m.contains("HTTP 503")
                            || m.contains("HTTP 404")
                            || m.contains("HTTP 429");
                    if (!tentarProximo) throw e;
                }
            }

            if (resposta == null) {
                throw new IOException("Os modelos do Gemini estão temporariamente indisponíveis. "
                        + "Tente novamente em instantes. Detalhe: "
                        + (ultimoErro == null ? "" : ultimoErro.getMessage()), ultimoErro);
            }
            return extrairTextoResposta(resposta);
        } catch (JSONException e) {
            throw new IOException("Erro ao montar ou interpretar o JSON da API.", e);
        }
    }

    private String postJson(String endpoint, JSONObject body) throws IOException {
        IOException ultimoErro = null;
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            HttpURLConnection conexao = null;
            try {
                conexao = (HttpURLConnection) new URL(endpoint).openConnection();
                conexao.setRequestMethod("POST");
                conexao.setConnectTimeout(TEMPO_CONEXAO_MS);
                conexao.setReadTimeout(TEMPO_RESPOSTA_MS);
                conexao.setDoOutput(true);
                conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conexao.setRequestProperty("x-goog-api-key", chaveApi);
                try (OutputStream saida = conexao.getOutputStream()) {
                    saida.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
                int status = conexao.getResponseCode();
                InputStream fluxo = status >= 200 && status < 300
                        ? conexao.getInputStream() : conexao.getErrorStream();
                String resposta = lerResposta(fluxo);
                if (status >= 200 && status < 300) return resposta;
                IOException erro = new IOException("Erro na API Gemini (HTTP " + status
                        + "): " + extrairMensagemErro(resposta)
                        + ((status == 429 || status >= 500)
                        ? " (tentativa " + tentativa + " de " + MAX_TENTATIVAS + ")" : ""));
                if (!(status == 429 || status >= 500) || tentativa == MAX_TENTATIVAS) {
                    throw erro;
                }
                ultimoErro = erro;
            } catch (IOException e) {
                String mensagem = e.getMessage();
                if (conexao == null || tentativa == MAX_TENTATIVAS || mensagem == null
                        || !mensagem.contains("HTTP 429")
                        && !mensagem.contains("HTTP 5")) throw e;
                ultimoErro = e;
            } finally {
                if (conexao != null) conexao.disconnect();
            }
            try {
                Thread.sleep(700L * (1L << (tentativa - 1)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("A tentativa de conexão com o Gemini foi interrompida.", e);
            }
        }
        throw ultimoErro == null ? new IOException("Falha ao conectar ao Gemini.") : ultimoErro;
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
                if (parte.optBoolean("thought", false)) {
                    continue;
                }
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