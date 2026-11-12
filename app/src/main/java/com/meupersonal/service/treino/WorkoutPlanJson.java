package com.meupersonal.service.treino;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class WorkoutPlanJson {
    private WorkoutPlanJson() { }

    public static JSONObject parse(String response) throws JSONException {
        if (response == null || response.trim().isEmpty()) {
            throw new JSONException("A IA retornou uma resposta vazia.");
        }
        String json = response.trim();
        if (json.startsWith("```")) {
            json = json.replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "");
        }
        JSONObject root = new JSONObject(json);
        JSONArray days = root.optJSONArray("dias");
        if (days == null || days.length() == 0) {
            throw new JSONException("O plano não contém dias de treino.");
        }
        for (int i = 0; i < days.length(); i++) {
            JSONObject day = days.optJSONObject(i);
            JSONArray exercises = day == null ? null : day.optJSONArray("exercicios");
            if (exercises == null || exercises.length() == 0) {
                throw new JSONException("Um dos dias não contém exercícios.");
            }
            for (int j = 0; j < exercises.length(); j++) {
                JSONObject exercise = exercises.optJSONObject(j);
                if (exercise == null || exercise.optString("nome").trim().isEmpty()) {
                    throw new JSONException("A IA retornou um exercício sem nome.");
                }
                if (exercise.optInt("series") <= 0 || repetitions(exercise.opt("repeticoes")) <= 0) {
                    throw new JSONException("Um exercício possui séries ou repetições inválidas.");
                }
            }
        }
        return root;
    }

    public static int repetitions(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value == null) return 0;
        String digits = String.valueOf(value).replaceAll("[^0-9].*$", "");
        try { return Integer.parseInt(digits); }
        catch (NumberFormatException ignored) { return 0; }
    }
}
