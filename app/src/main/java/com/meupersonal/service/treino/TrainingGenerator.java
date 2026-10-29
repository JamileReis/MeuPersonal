package com.meupersonal.service.treino;

import com.meupersonal.model.questionario.GoalType;
import com.meupersonal.model.questionario.Questionnaire;

import com.meupersonal.model.treino.Exercise;
import com.meupersonal.model.treino.MuscleGroup;
import com.meupersonal.model.treino.PlanStatus;
import com.meupersonal.model.treino.Workout;
import com.meupersonal.model.treino.WorkoutPlan;
import com.meupersonal.service.ia.AiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    WorkoutPlan buildPlan(
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

        WorkoutPlan plan = new WorkoutPlan(null, questionnaire.getUserId(),
                name, objective, new Date(), PlanStatus.INATIVO);
        List<Workout> workouts = new ArrayList<>();
        for (int dayIndex = 0; dayIndex < days.length(); dayIndex++) {
            JSONObject day = days.optJSONObject(dayIndex);
            if (day == null) throw new IOException("Uma sessão do plano está malformada.");
            String workoutName = firstNonBlank(day.optString("nome", null),
                    day.optString("dia", null));
            if (workoutName == null) workoutName = "Treino " + (char) ('A' + dayIndex);
            JSONArray exerciseItems = day.optJSONArray("exercicios");
            if (exerciseItems == null || exerciseItems.length() == 0) {
                throw new IOException("A sessão " + workoutName + " não contém exercícios.");
            }
            List<Exercise> exercises = new ArrayList<>();
            for (int exerciseIndex = 0; exerciseIndex < exerciseItems.length(); exerciseIndex++) {
                JSONObject item = exerciseItems.optJSONObject(exerciseIndex);
                if (item == null) throw new IOException("Um exercício está malformado.");
                String exerciseName = firstNonBlank(item.optString("nome", null),
                        item.optString("name", null));
                int sets = item.optInt("series", 0);
                Integer reps = parseRepetitions(item.opt("repeticoes"));
                int rest = item.optInt("descansoSegundos", 0);
                String muscleText = firstNonBlank(item.optString("grupoMuscular", null),
                        item.optString("grupo", null));
                Exercise exercise = new Exercise(null, exerciseName,
                        item.optString("descricao", ""), parseMuscleGroup(muscleText),
                        item.optString("instrucao", item.optString("instrucoes", "")),
                        sets > 0 ? sets : null, reps, rest >= 0 ? rest : null,
                        item.optString("intensidade", ""),
                        item.has("carga") && item.opt("carga") instanceof Number
                                ? item.optDouble("carga") : null);
                exercises.add(exercise);
            }
            Workout workout = new Workout(null, exercises,
                    parseDay(day.optString("dia", ""), dayIndex),
                    day.optInt("duracaoMinutos", 0), day.optString("descricao", ""),
                    workoutName, null);
            workouts.add(workout);
            plan.addWorkout(workout);
        }
        ValidationResult result = validator.validatePlan(plan, workouts);
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
        String normalized = normalize(value).replace(' ', '_').replace('-', '_');
        switch (normalized) {
            case "EMAGRECIMENTO":
            case "PERDA_DE_PESO":
            case "WEIGHT_LOSS": return GoalType.PERDA_DE_PESO;
            case "HIPERTROFIA":
            case "MUSCLE_GAIN": return GoalType.HIPERTROFIA;
            case "FORCA":
            case "STRENGTH": return GoalType.FORCA;
            case "CONDICIONAMENTO":
            case "CONDICIONAMENTO_FISICO":
            case "RESISTENCIA":
            case "ENDURANCE":
            case "CONDITIONING": return GoalType.CONDICIONAMENTO;
            case "MOBILIDADE":
            case "MOBILITY": return GoalType.MOBILIDADE;
            case "SAUDE":
            case "BEM_ESTAR":
            case "SAUDE_E_BEM_ESTAR":
            case "SAUDE_GERAL":
            case "GENERAL_HEALTH": return GoalType.SAUDE_GERAL;
            default:
                try { return GoalType.valueOf(normalized); }
                catch (IllegalArgumentException ignored) { return null; }
        }
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toUpperCase(Locale.ROOT);
    }

    private MuscleGroup parseMuscleGroup(String value) {
        String group = normalize(value).replace(' ', '_');
        if (group.contains("PEITO") || group.contains("CHEST")) return MuscleGroup.PEITO;
        if (group.contains("COSTAS") || group.contains("BACK")) return MuscleGroup.COSTAS;
        if (group.contains("PERNA") || group.contains("LEG")) return MuscleGroup.PERNAS;
        if (group.contains("OMBRO") || group.contains("SHOULDER")) return MuscleGroup.OMBROS;
        if (group.contains("BRACO") || group.contains("BICEPS") || group.contains("TRICEPS")) return MuscleGroup.BRAÇOS;
        if (group.contains("CORE") || group.contains("ABDOM")) return MuscleGroup.CORE;
        if (group.contains("CORPO_INTEIRO") || group.contains("FULL_BODY")) return MuscleGroup.CORPO_INTEIRO;
        return MuscleGroup.OUTRO;
    }

    private DayOfWeek parseDay(String value, int index) {
        String day = normalize(value);
        if (day.contains("SEGUNDA") || day.contains("MONDAY")) return DayOfWeek.MONDAY;
        if (day.contains("TERCA") || day.contains("TUESDAY")) return DayOfWeek.TUESDAY;
        if (day.contains("QUARTA") || day.contains("WEDNESDAY")) return DayOfWeek.WEDNESDAY;
        if (day.contains("QUINTA") || day.contains("THURSDAY")) return DayOfWeek.THURSDAY;
        if (day.contains("SEXTA") || day.contains("FRIDAY")) return DayOfWeek.FRIDAY;
        if (day.contains("SABADO") || day.contains("SATURDAY")) return DayOfWeek.SATURDAY;
        if (day.contains("DOMINGO") || day.contains("SUNDAY")) return DayOfWeek.SUNDAY;
        return DayOfWeek.of(Math.floorMod(index, 7) + 1);
    }

    private Integer parseRepetitions(Object raw) {
        if (raw instanceof Number) return ((Number) raw).intValue();
        if (raw == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)")
                .matcher(raw.toString());
        if (!matcher.find()) return null;
        try { return Integer.parseInt(matcher.group(1)); }
        catch (NumberFormatException ignored) { return null; }
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