package com.meupersonal.service.ml;

import java.util.Locale;
import java.util.Objects;

public class SentimentAnalysisService {
    private static final String PROMPT_ANALISE =
            "Analise o sentimento do feedback recebido pelo aplicativo "
                    + "MeuPersonal. Identifique o sentimento predominante, "
                    + "explique brevemente o motivo e responda em português "
                    + "do Brasil. Não invente informações.\n\n"
                    + "Feedback:";

    private static final String PROMPT_CATEGORIZACAO =
            "Categorize o feedback recebido pelo aplicativo MeuPersonal. "
                    + "Escolha exclusivamente uma das seguintes categorias:\n"
                    + "POSITIVO: elogios ou experiências positivas.\n"
                    + "NEGATIVO: reclamações ou experiências negativas.\n"
                    + "SUGESTAO: ideias ou propostas de melhoria.\n"
                    + "ERRO: falhas técnicas ou problemas de funcionamento.\n"
                    + "OUTRO: feedback que não se enquadre nas categorias "
                    + "anteriores.\n\n"
                    + "Responda somente com o nome da categoria, "
                    + "sem explicações.\n\n"
                    + "Feedback:";

    private final AiModelService servicoModeloIA;

    public SentimentAnalysisService(AiModelService servicoModeloIA) {
        this.servicoModeloIA = Objects.requireNonNull(
                servicoModeloIA,
                "O serviço de modelo de IA não pode ser nulo."
        );
    }

    public String analisarFeedback(String feedback) {
        validarFeedback(feedback);
        return analisar(PROMPT_ANALISE, feedback);
    }

    public String categorizarFeedback(String feedback) {
        validarFeedback(feedback);

        String categoria = analisar(PROMPT_CATEGORIZACAO, feedback)
                .trim()
                .toUpperCase(Locale.ROOT);

        categoria = categoria.replaceAll(
                "^[\"'`\\s]+|[\"'`\\s.,;:]+$",
                ""
        );

        switch (categoria) {
            case "POSITIVO":
                return "POSITIVO";
            case "NEGATIVO":
                return "NEGATIVO";
            case "SUGESTAO":
            case "SUGESTÃO":
                return "SUGESTAO";
            case "ERRO":
                return "ERRO";
            case "OUTRO":
                return "OUTRO";
            default:
                return "OUTRO";
        }
    }

    private String analisar(String instrucao, String feedback) {
        String promptCompleto = instrucao + "\n\n" + feedback;
        return servicoModeloIA.gerarTexto(promptCompleto);
    }

    private void validarFeedback(String feedback) {
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O feedback não pode ser nulo ou vazio."
            );
        }
    }
}