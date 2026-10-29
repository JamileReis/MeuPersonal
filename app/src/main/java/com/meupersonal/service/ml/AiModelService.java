package com.meupersonal.service.ml;

import com.meupersonal.model.questionario.Questionnaire;
import com.meupersonal.service.ia.AiService;

import java.io.IOException;
import java.util.Objects;

public class AiModelService {
    private static final double TEMPERATURA_PADRAO = 0.7;
    private static final int MAX_TOKENS_PADRAO = 2048;

    private final String provedorApi;
    private final String nomeModelo;
    private final String instrucao;
    private final double temperatura;
    private final int maxTokens;
    private final AiService servicoIA;

    public AiModelService(
            String provedorApi,
            String nomeModelo,
            String instrucao,
            AiService servicoIA
    ) {
        this(
                provedorApi,
                nomeModelo,
                instrucao,
                TEMPERATURA_PADRAO,
                MAX_TOKENS_PADRAO,
                servicoIA
        );
    }

    public AiModelService(
            String provedorApi,
            String nomeModelo,
            String instrucao,
            double temperatura,
            int maxTokens,
            AiService servicoIA
    ) {
        this.provedorApi = Objects.requireNonNull(
                provedorApi, "O provedor da API não pode ser nulo."
        );

        this.nomeModelo = Objects.requireNonNull(
                nomeModelo, "O nome do modelo não pode ser nulo."
        );

        this.instrucao = Objects.requireNonNull(
                instrucao, "A instrução não pode ser nula."
        );

        this.servicoIA = Objects.requireNonNull(
                servicoIA, "O serviço de IA não pode ser nulo."
        );

        if (temperatura < 0.0 || temperatura > 2.0) {
            throw new IllegalArgumentException(
                    "A temperatura deve estar entre 0 e 2."
            );
        }

        if (maxTokens <= 0) {
            throw new IllegalArgumentException(
                    "O limite de tokens deve ser maior que zero."
            );
        }

        this.temperatura = temperatura;
        this.maxTokens = maxTokens;
    }

    public String gerarTexto(String entrada) {
        validarEntrada(entrada);

        String prompt = construirPrompt(entrada);

        return enviarPrompt(prompt);
    }

    public String gerarRespostaEstruturada(String entrada) {
        validarEntrada(entrada);

        String instrucaoEstruturada =
                "Retorne somente um JSON válido, sem blocos Markdown "
                        + "ou texto adicional.";

        String prompt = construirPrompt(
                entrada + "\n\n" + instrucaoEstruturada
        );

        return enviarPrompt(prompt);
    }

    private String construirPrompt(String entrada) {
        return instrucao + "\n\n" + entrada;
    }

    private String enviarPrompt(String prompt) {
        try {
            return servicoIA.sendPrompt(prompt);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível consultar o serviço de IA.",
                    e
            );
        }
    }

    private void validarEntrada(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "A entrada não pode ser nula ou vazia."
            );
        }
    }

    public String getProvedorApi() {
        return provedorApi;
    }

    public String getNomeModelo() {
        return nomeModelo;
    }

    public String getInstrucao() {
        return instrucao;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public int getMaxTokens() {
        return maxTokens;
    }
}