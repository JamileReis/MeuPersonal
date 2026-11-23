package com.meupersonal.service.treino;

import android.content.Context;
import android.content.SharedPreferences;

import com.meupersonal.data.LocalWorkoutStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class StarterWorkoutSeeder {
    private static final String PREFS = "meu_personal_defaults";
    private static final String SEEDED_KEY = "starter_workout_seeded";

    private StarterWorkoutSeeder() {
    }

    public static synchronized void ensureAvailable(
            Context context,
            LocalWorkoutStore store
    ) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences.getBoolean(SEEDED_KEY, false)) {
            return;
        }

        if (!store.listPlans().isEmpty()) {
            preferences.edit().putBoolean(SEEDED_KEY, true).apply();
            return;
        }

        try {
            JSONObject plan = starterPlan();
            store.savePlan(
                    plan.optString("titulo"),
                    plan.optString("objetivo"),
                    plan.toString(),
                    System.currentTimeMillis()
            );
            preferences.edit().putBoolean(SEEDED_KEY, true).apply();
        } catch (JSONException error) {
            throw new IllegalStateException("Não foi possível preparar o treino inicial.", error);
        }
    }

    private static JSONObject starterPlan() throws JSONException {
        JSONArray exercises = new JSONArray()
                .put(exercise(
                        "Sentar e levantar da cadeira",
                        "Pernas e glúteos",
                        "8–10",
                        "Sente-se numa cadeira firme e levante-se com controle. Use as mãos como apoio se precisar; mantenha a cadeira encostada na parede.",
                        "Movimento confortável, sem pressa. Pare se sentir dor, tontura ou falta de ar fora do normal."
                ))
                .put(exercise(
                        "Flexão na parede",
                        "Peito e braços",
                        "8–10",
                        "Fique de frente para uma parede, mãos apoiadas na altura do peito. Flexione os cotovelos aproximando o corpo e empurre a parede para voltar.",
                        "Mantenha o corpo alinhado e faça apenas uma amplitude confortável."
                ))
                .put(exercise(
                        "Elevação de quadril",
                        "Glúteos e posterior da coxa",
                        "8–10",
                        "Deite-se de costas com joelhos flexionados e pés apoiados. Contraia levemente o abdômen e eleve o quadril sem arquear a lombar.",
                        "Suba e desça devagar; não force a amplitude."
                ))
                .put(exercise(
                        "Elevação de panturrilha com apoio",
                        "Panturrilhas",
                        "10–12",
                        "Em pé, segure uma cadeira firme ou bancada. Eleve os calcanhares e retorne ao chão de forma controlada.",
                        "Use o apoio para equilíbrio; não faça o movimento se causar dor ou instabilidade."
                ));

        JSONObject day = new JSONObject()
                .put("dia", "Treino base")
                .put("nome", "Corpo inteiro")
                .put("duracaoMinutos", 20)
                .put("descricao", "Treino inicial simples, sem equipamentos. Faça 3–5 minutos de movimentos leves antes de começar e descanse 60–90 segundos entre as séries. Comece devagar e ajuste os movimentos ao seu conforto.")
                .put("exercicios", exercises);

        return new JSONObject()
                .put("titulo", "Treino inicial — corpo inteiro")
                .put("objetivo", "Plano base sem equipamentos para você já ter uma opção de treino.")
                .put("observacoes", "Este é um modelo geral, não uma prescrição individual. Se você tem lesão, condição de saúde, dor ou restrição de movimento, procure orientação profissional antes de iniciar.")
                .put("dias", new JSONArray().put(day));
    }

    private static JSONObject exercise(
            String name,
            String group,
            String reps,
            String instruction,
            String description
    ) throws JSONException {
        return new JSONObject()
                .put("nome", name)
                .put("grupoMuscular", group)
                .put("series", 2)
                .put("repeticoes", reps)
                .put("descansoSegundos", 60)
                .put("intensidade", "Leve a moderada; mantenha ritmo confortável")
                .put("carga", 0)
                .put("tipoCarga", "PESO_CORPORAL")
                .put("observacaoCarga", "Sem carga externa")
                .put("instrucao", instruction)
                .put("descricao", description);
    }
}
