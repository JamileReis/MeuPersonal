package com.meupersonal.presentation.treino;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.meupersonal.R;
import com.meupersonal.data.LocalWorkoutStore;
import com.meupersonal.data.auth.SecureSessionStore;
import com.meupersonal.model.auth.AuthSession;
import com.meupersonal.service.SupabaseDataService;
import com.meupersonal.service.ia.GeminiAiService;
import com.meupersonal.service.treino.WorkoutPlanJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PromptTreinoFragment extends Fragment{
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public PromptTreinoFragment() {
        super(R.layout.fragment_prompt_treino);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle state
    ) {
        super.onViewCreated(view, state);

        EditText goal = view.findViewById(R.id.inputObjetivo);
        EditText frequency = view.findViewById(R.id.inputFrequencia);
        EditText duration = view.findViewById(R.id.inputDuracao);
        EditText level = view.findViewById(R.id.inputNivel);
        EditText equipment = view.findViewById(R.id.inputEquipamentos);
        EditText restrictions = view.findViewById(R.id.inputLimitacoes);
        EditText preferences = view.findViewById(R.id.inputPreferencias);

        Button generate = view.findViewById(R.id.btnGerarTreino);
        TextView error = view.findViewById(R.id.textErroGeracao);

        SharedPreferences profile = requireContext().getSharedPreferences(
                "meu_personal_profile", Context.MODE_PRIVATE);
        if (goal.getText().toString().trim().isEmpty()) {
            goal.setText(profile.getString("objetivo", ""));
        }
        if (preferences.getText().toString().trim().isEmpty()) {
            preferences.setText(profile.getString("preferencias", ""));
        }

        generate.setOnClickListener(v -> {
            String objective = value(goal);
            String days = value(frequency);
            String minutes = value(duration);

            if (objective.isEmpty() || days.isEmpty() || minutes.isEmpty()) {
                showError(error, "Informe objetivo, dias por semana e duração.");
                return;
            }

            int count;
            int sessionMinutes;

            try {
                count = Integer.parseInt(days);
                sessionMinutes = Integer.parseInt(minutes);

                if (count < 1 || count > 7
                        || sessionMinutes < 10
                        || sessionMinutes > 240) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                showError(error,
                        "Use de 1 a 7 dias e duração entre 10 e 240 minutos.");
                return;
            }

            String prompt = buildPrompt(
                    objective,
                    count,
                    sessionMinutes,
                    value(level),
                    value(equipment),
                    profile.getString("limitacoes", ""),
                    value(restrictions),
                    value(preferences)
            );

            Context appContext =
                    requireContext().getApplicationContext();

            generate.setEnabled(false);
            generate.setText("Gerando seu treino...");
            error.setVisibility(View.GONE);

            executor.execute(() -> {
                try {
                    String response = new GeminiAiService()
                            .generateTrainingPlanFromPrompt(prompt);

                    JSONObject plan = WorkoutPlanJson.parse(response);

                    String title = plan.optString(
                            "titulo",
                            "Meu plano personalizado"
                    );

                    String objectiveText = plan.optString(
                            "objetivo",
                            objective
                    );

                    long createdAt = System.currentTimeMillis();
                    String remoteClientId = UUID.randomUUID().toString();
                    LocalWorkoutStore localStore = new LocalWorkoutStore(appContext);
                    long localPlanId = localStore.savePlan(
                            title,
                            objectiveText,
                            plan.toString(),
                            createdAt,
                            remoteClientId
                    );

                    AuthSession session = new SecureSessionStore(appContext).read();
                    if (session != null) {
                        try {
                            new SupabaseDataService().syncPlan(
                                    session,
                                    remoteClientId,
                                    title,
                                    objectiveText,
                                    plan.toString(),
                                    createdAt
                            );
                            localStore.markPlanSynced(localPlanId);
                        } catch (IOException remoteError) {
                            android.util.Log.w(
                                    "MeuPersonal",
                                    "Treino salvo localmente, mas não sincronizado agora.",
                                    remoteError
                            );
                        }
                    }

                    mainHandler.post(() -> {
                        if (!isAdded()) {
                            return;
                        }

                        NavHostFragment.findNavController(this)
                                .popBackStack();

                        NavHostFragment.findNavController(this)
                                .navigate(R.id.treinoDiaFragment);
                    });

                } catch (IOException | JSONException | RuntimeException exception) {
                    mainHandler.post(() -> {
                        if (!isAdded()) {
                            return;
                        }

                        generate.setEnabled(true);
                        generate.setText("Gerar meu treino com IA");

                        showError(
                                error,
                                exception.getMessage() == null
                                        ? "Não foi possível gerar o treino."
                                        : exception.getMessage()
                        );
                    });
                }
            });
        });
    }

    private String buildPrompt(
            String objective,
            int days,
            int minutes,
            String level,
            String equipment,
            String permanentRestrictions,
            String dailyCondition,
            String preferences
    ) {
        return "Você é o treinador virtual do MeuPersonal. "
                + "Crie um plano individualizado e executável.\n\n"

                + "DADOS DO TREINAMENTO:\n"
                + "- Objetivo: " + objective + "\n"
                + "- Frequência semanal: " + days + " dias\n"
                + "- Duração: " + minutes + " minutos\n"
                + "- Experiência: " + fallback(level, "Iniciante") + "\n"
                + "- Equipamentos disponíveis: " + fallback(equipment, "Não informado") + "\n"
                + "- Restrições permanentes do perfil: "
                + fallback(permanentRestrictions, "Nenhuma cadastrada") + "\n"
                + "- Condições relatadas hoje: "
                + fallback(dailyCondition, "Nenhuma relatada") + "\n"
                + "- Preferências: " + fallback(preferences, "Nenhuma informada") + "\n\n"

                + "PRESCRIÇÃO DE CARGA:\n"
                + "1. Não deixe a carga nula quando for possível recomendar "
                + "uma carga inicial conservadora.\n"
                + "2. Para exercícios com pesos, informe um valor numérico "
                + "em quilogramas no campo carga.\n"
                + "3. Considere objetivo, exercício, experiência, equipamento "
                + "e limitações informadas.\n"
                + "4. Não invente histórico de levantamento, recordes ou "
                + "capacidade física do usuário.\n"
                + "5. Quando não houver histórico de carga, recomende uma "
                + "carga inicial de calibração baixa e explique como ajustá-la.\n"
                + "6. Para exercícios com peso corporal, utilize carga igual "
                + "a zero e tipoCarga igual a PESO_CORPORAL.\n"
                + "7. Para exercícios com halteres, informe a carga por halter "
                + "em observacaoCarga.\n"
                + "8. Para exercícios com barra, informe a carga total, incluindo "
                + "a barra, quando esse dado for conhecido.\n"
                + "9. Não prescreva cargas máximas nem testes de uma repetição máxima.\n"
                + "10. Inclua uma orientação objetiva de progressão em "
                + "observacaoCarga.\n\n"

                + "INTERPRETAÇÃO DO PEDIDO:\n"
                + "Interprete a solicitação em português brasileiro como texto livre, "
                + "incluindo acentos, expressões informais, combinações de grupos musculares "
                + "e modalidades como musculação e aeróbico. Não exija correspondência com "
                + "uma lista fechada de palavras-chave. Se equipamentos não forem informados, "
                + "não presuma que o usuário os possui; ofereça alternativas com peso corporal "
                + "ou indique claramente quando um equipamento é necessário.\n\n"
                + "SEGURANÇA:\n"
                + "Respeite obrigatoriamente as restrições permanentes do perfil e as "
                + "condições temporárias informadas hoje. Não prescreva exercícios "
                + "incompatíveis com elas. Quando houver restrição relevante, "
                + "adapte o exercício ou explique a substituição. Não faça "
                + "diagnósticos médicos.\n\n"

                + "FORMATO OBRIGATÓRIO:\n"
                + "Retorne exclusivamente JSON válido, sem Markdown.\n"
                + "{"
                + "\"titulo\":\"\","
                + "\"objetivo\":\"\","
                + "\"observacoes\":\"\","
                + "\"dias\":[{"
                + "\"dia\":\"Segunda-feira\","
                + "\"nome\":\"Treino A\","
                + "\"duracaoMinutos\":" + minutes + ","
                + "\"descricao\":\"\","
                + "\"exercicios\":[{"
                + "\"nome\":\"\","
                + "\"grupoMuscular\":\"\","
                + "\"series\":3,"
                + "\"repeticoes\":8,"
                + "\"descansoSegundos\":90,"
                + "\"intensidade\":\"\","
                + "\"carga\":0,"
                + "\"tipoCarga\":\"PESO_CORPORAL\","
                + "\"observacaoCarga\":\"\","
                + "\"instrucao\":\"\","
                + "\"descricao\":\"\""
                + "}]"
                + "}]}"

                + "\n\nREGRAS FINAIS:\n"
                + "- Gere exatamente " + days + " sessões.\n"
                + "- Cada sessão deve conter de 4 a 8 exercícios.\n"
                + "- Respeite a duração solicitada.\n"
                + "- Utilize séries, repetições e descanso positivos.\n"
                + "- Informe carga numérica e unidade em kg quando aplicável.\n"
                + "- Não use null como substituto de uma recomendação possível.\n"
                + "- Não invente dados pessoais ausentes.\n"
                + "- Retorne todos os campos do JSON.";
    }

    private String value(EditText input) {
        return input.getText() == null
                ? ""
                : input.getText().toString().trim();
    }

    private String fallback(String value, String defaultValue) {
        return value == null || value.trim().isEmpty()
                ? defaultValue
                : value.trim();
    }

    private void showError(TextView error, String message) {
        error.setText(message);
        error.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
