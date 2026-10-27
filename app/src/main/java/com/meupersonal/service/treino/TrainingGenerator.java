package com.meupersonal.service.treino;

import com.meupersonal.model.questionario.GoalType;
import com.meupersonal.model.questionario.Questionnaire;

import com.meupersonal.model.treino.PlanStatus;
import com.meupersonal.model.treino.WorkoutPlan;
import com.meupersonal.service.ia.AiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class TrainingGenerator {

    private final AiService aiService;
    private final TrainingPlanValidator validator;

    public TrainingGenerator(
            AiService aiService,
            TrainingPlanValidator validator) {

        if (aiService == null) {
            throw new IllegalArgumentException(
                    "AiService não pode ser nulo."
            );
        }

        if (validator == null) {
            throw new IllegalArgumentException(
                    "TrainingPlanValidator não pode ser nulo."
            );
        }

        this.aiService = aiService;
        this.validator = validator;
    }

    public WorkoutPlan generate(Questionnaire questionnaire)
            throws IOException {

        ValidationResult questionnaireResult =
                validator.validateQuestionnaire(questionnaire);

        if (!questionnaireResult.isValid()) {
            throw new IllegalArgumentException(
                    String.join("; ", questionnaireResult.getMessages())
            );
        }

        String response = aiService.generateTrainingPlan(questionnaire);

        return buildPlan(questionnaire, response);
    }

    private WorkoutPlan buildPlan(
            Questionnaire questionnaire,
            String response) throws IOException {

        JSONObject json = parseJson(response);

        JSONArray missingData = json.optJSONArray("dadosFaltantes");

        if (missingData != null && missingData.length() > 0) {
            throw new IOException(
                    "Dados necessários ausentes: " + join(missingData)
            );
        }

        String name = firstNonBlank(
                json.optString("titulo", null),
                json.optString("nome", null)
        );

        if (name == null) {
            throw new IOException(
                    "A resposta da IA não contém o nome do plano."
            );
        }

        String objectiveValue = firstNonBlank(
                json.optString("objetivo", null),
                json.optString("goal", null)
        );

        GoalType objective = parseGoal(objectiveValue);

        if (objective == null) {
            throw new IOException(
                    "A resposta da IA não contém um objetivo válido."
            );
        }

        JSONArray days = json.optJSONArray("dias");

        if (days == null || days.length() == 0) {
            throw new IOException(
                    "A resposta da IA não contém treinos em 'dias'."
            );
        }

        WorkoutPlan plan = new WorkoutPlan(
                null,
                questionnaire.getUserId(),
                name,
                objective,
                new Date(),
                PlanStatus.INATIVO
        );

        ValidationResult result = validator.validatePlan(plan);

        if (!result.isValid()) {
            throw new IOException(
                    "Plano inválido: "
                            + String.join("; ", result.getMessages())
            );
        }

        return plan;
    }

    private JSONObject parseJson(String response) throws IOException {
        if (response == null || response.trim().isEmpty()) {
            throw new IOException(
                    "O serviço de IA retornou uma resposta vazia."
            );
        }

        String normalized = response.trim();

        if (normalized.startsWith("```")) {
            normalized = normalized
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "");
        }

        try {
            return new JSONObject(normalized);
        } catch (JSONException exception) {
            throw new IOException(
                    "Não foi possível interpretar o JSON retornado pela IA.",
                    exception
            );
        }
    }

    private GoalType parseGoal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');

        try {
            return GoalType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            switch (normalized) {
                case "PERDA_DE_PESO":
                case "WEIGHT_LOSS":
                    return findGoal("WEIGHT_LOSS");

                case "HIPERTROFIA":
                case "MUSCLE_GAIN":
                    return findGoal("MUSCLE_GAIN");

                case "FORCA":
                case "STRENGTH":
                    return findGoal("STRENGTH");

                case "CONDICIONAMENTO":
                case "CONDITIONING":
                    return findGoal("CONDITIONING");

                case "RESISTENCIA":
                case "ENDURANCE":
                    return findGoal("ENDURANCE");

                case "MOBILIDADE":
                case "MOBILITY":
                    return findGoal("MOBILITY");

                case "SAUDE_GERAL":
                case "GENERAL_HEALTH":
                    return findGoal("GENERAL_HEALTH");

                default:
                    return null;
            }
        }
    }

    private GoalType findGoal(String name) {
        for (GoalType goal : GoalType.values()) {
            if (goal.name().equalsIgnoreCase(name)) {
                return goal;
            }
        }

        return null;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null
                && !first.trim().isEmpty()
                && !"null".equalsIgnoreCase(first.trim())) {
            return first.trim();
        }

        if (second != null
                && !second.trim().isEmpty()
                && !"null".equalsIgnoreCase(second.trim())) {
            return second.trim();
        }

        return null;
    }

    private String join(JSONArray values) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < values.length(); i++) {
            String value = values.optString(i, "").trim();

            if (value.isEmpty()) {
                continue;
            }

            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(value);
        }

        return result.toString();
    }
}