package com.meupersonal.presentation.treino;

import android.os.Bundle;
import android.content.Context;
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
import com.meupersonal.service.ia.GeminiAiService;
import com.meupersonal.service.treino.WorkoutPlanJson;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public final class PromptTreinoFragment extends Fragment{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

public PromptTreinoFragment() { super(R.layout.fragment_prompt_treino); }

@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
    EditText goal = view.findViewById(R.id.inputObjetivo);
    EditText frequency = view.findViewById(R.id.inputFrequencia);
    EditText duration = view.findViewById(R.id.inputDuracao);
    EditText level = view.findViewById(R.id.inputNivel);
    EditText equipment = view.findViewById(R.id.inputEquipamentos);
    EditText restrictions = view.findViewById(R.id.inputLimitacoes);
    EditText preferences = view.findViewById(R.id.inputPreferencias);
    Button generate = view.findViewById(R.id.btnGerarTreino);
    TextView error = view.findViewById(R.id.textErroGeracao);

    generate.setOnClickListener(v -> {
        String objective = value(goal);
        String days = value(frequency);
        String minutes = value(duration);
        if (objective.isEmpty() || days.isEmpty() || minutes.isEmpty()) {
            error.setText("Informe objetivo, dias por semana e duração.");
            error.setVisibility(View.VISIBLE);
            return;
        }
        int count;
        int sessionMinutes;
        try {
            count = Integer.parseInt(days);
            sessionMinutes = Integer.parseInt(minutes);
            if (count < 1 || count > 7 || sessionMinutes < 10 || sessionMinutes > 240) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            error.setText("Use de 1 a 7 dias e duração entre 10 e 240 minutos.");
            error.setVisibility(View.VISIBLE);
            return;
        }

        String prompt = buildPrompt(objective, count, sessionMinutes, value(level),
                value(equipment), value(restrictions), value(preferences));
        Context appContext = requireContext().getApplicationContext();
        generate.setEnabled(false);
        generate.setText("Gerando seu treino...");
        error.setVisibility(View.GONE);
        executor.execute(() -> {
            try {
                String response = new GeminiAiService().generateTrainingPlanFromPrompt(prompt);
                JSONObject plan = WorkoutPlanJson.parse(response);
                String title = plan.optString("titulo", "Meu plano personalizado");
                String objectiveText = plan.optString("objetivo", objective);
                new LocalWorkoutStore(appContext).savePlan(
                        title, objectiveText, plan.toString(), System.currentTimeMillis());
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    NavHostFragment.findNavController(this).popBackStack();
                    NavHostFragment.findNavController(this).navigate(R.id.treinoDiaFragment);
                });
            } catch (IOException | JSONException | RuntimeException exception) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    generate.setEnabled(true);
                    generate.setText("Gerar meu treino com IA");
                    error.setText(exception.getMessage() == null
                            ? "Não foi possível gerar o treino." : exception.getMessage());
                    error.setVisibility(View.VISIBLE);
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
            String restrictions,
            String preferences
    ) {
        return "Você é um personal trainer virtual do aplicativo MeuPersonal. "
                + "Sua função é executar as decisões de treinamento do usuário e "
                + "transformá-las em um plano personalizado.\n\n"

                + "DADOS DO TREINAMENTO:\n"
                + "- Objetivo: " + objective + "\n"
                + "- Dias por semana: " + days + "\n"
                + "- Duração por sessão: " + minutes + " minutos\n"
                + "- Nível: " + fallback(level, "Não informado") + "\n"
                + "- Equipamentos: " + fallback(equipment, "Não informado") + "\n"
                + "- Limitações: " + fallback(restrictions, "Nenhuma informada") + "\n\n"

                + "COMANDOS E DECISÕES DO USUÁRIO:\n"
                + (preferences.isEmpty()
                ? "Nenhum comando adicional informado.\n"
                : preferences + "\n")

                + "\nREGRAS OBRIGATÓRIAS:\n"
                + "1. O usuário tem liberdade para decidir como deseja treinar.\n"
                + "2. Trate os comandos escritos como instruções a serem executadas, "
                + "não como sugestões.\n"
                + "3. Respeite a divisão muscular solicitada pelo usuário.\n"
                + "4. Respeite os exercícios escolhidos e os exercícios que ele "
                + "não deseja realizar.\n"
                + "5. Permita combinar musculação, aeróbico, mobilidade e "
                + "condicionamento físico.\n"
                + "6. Não imponha divisões ABC, ABCD ou ABCDE se o usuário "
                + "solicitar outra organização.\n"
                + "7. Não substitua decisões do usuário por preferências próprias.\n"
                + "8. Interprete linguagem natural, comandos informais e textos "
                + "com erros gramaticais.\n"
                + "9. Combine os comandos com os dados do formulário.\n"
                + "10. Não ignore instruções específicas.\n"
                + "11. Se duas instruções forem incompatíveis, explique o conflito "
                + "em observacoes.\n"
                + "12. Se uma solicitação apresentar risco físico, explique o "
                + "motivo e proponha uma alternativa segura.\n"
                + "13. Nunca invente informações sobre a saúde do usuário.\n"
                + "14. Respeite os equipamentos disponíveis e as limitações informadas.\n\n"

                + "FORMATO DE RESPOSTA:\n"
                + "Responda exclusivamente com JSON válido, sem Markdown ou "
                + "qualquer texto fora do JSON.\n"
                + "Use exatamente esta estrutura:\n"
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
                + "\"repeticoes\":12,"
                + "\"descansoSegundos\":60,"
                + "\"intensidade\":\"\","
                + "\"carga\":null,"
                + "\"instrucao\":\"\","
                + "\"descricao\":\"\""
                + "}]"
                + "}]}"

                + "\n\nREQUISITOS FINAIS:\n"
                + "- Gere exatamente " + days + " sessões.\n"
                + "- Cada sessão deve conter de 4 a 8 exercícios.\n"
                + "- Respeite a duração de " + minutes + " minutos por sessão.\n"
                + "- Execute todas as instruções compatíveis e seguras do usuário.\n"
                + "- Utilize números positivos para séries, repetições e descanso.\n"
                + "- Utilize null quando a carga não puder ser determinada.\n"
                + "- Retorne todos os campos exigidos no JSON.\n"
                + "- Garanta que o JSON possa ser interpretado pelo Android.";
    }

private String value(EditText input) {
    return input.getText() == null ? "" : input.getText().toString().trim();
}

private String fallback(String value, String defaultValue) {
    return value.isEmpty() ? defaultValue : value;
}

@Override
public void onDestroy() {
    executor.shutdownNow();
    super.onDestroy();
}
}

