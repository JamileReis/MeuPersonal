package com.meupersonal.presentation.treino;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.meupersonal.R;
import com.meupersonal.data.LocalWorkoutStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



    public final class TreinoDiaFragment extends Fragment {

        public TreinoDiaFragment() {
            super(R.layout.fragment_treino_dia);
        }

        @Override
        public void onViewCreated(
                @NonNull View view,
                @Nullable Bundle savedInstanceState
        ) {
            super.onViewCreated(view, savedInstanceState);

            TextView title = view.findViewById(R.id.textTituloTreino);
            LinearLayout exercisesContainer =
                    view.findViewById(R.id.containerExercicios);

            Button alter = view.findViewById(R.id.btnAlterarTreinoIa);
            Button finish = view.findViewById(R.id.btnFinalizarTreino);

            LocalWorkoutStore store =
                    new LocalWorkoutStore(requireContext());

            LocalWorkoutStore.PlanRecord record = store.getLatestPlan();

            alter.setOnClickListener(v ->
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_treino_to_prompt)
            );

            if (record == null) {
                title.setText("Seu treino personalizado");

                exercisesContainer.addView(createText(
                        "Você ainda não tem um plano. Personalize seu treino para gerar os exercícios.",
                        true
                ));

                finish.setEnabled(false);
                alter.setText("Criar meu treino com IA");
                return;
            }

            try {
                JSONObject plan = new JSONObject(record.json);

                title.setText(record.title);

                JSONArray days = plan.optJSONArray("dias");
                JSONObject today = chooseWorkout(days);

                if (today == null) {
                    throw new JSONException(
                            "O plano não possui uma sessão válida."
                    );
                }

                JSONArray exercises = today.optJSONArray("exercicios");

                if (exercises == null || exercises.length() == 0) {
                    throw new JSONException(
                            "Nenhum exercício foi encontrado nesta sessão."
                    );
                }

                for (int i = 0; i < exercises.length(); i++) {
                    JSONObject exercise = exercises.optJSONObject(i);

                    if (exercise == null) {
                        continue;
                    }

                    String name = exercise.optString("nome", "Exercício");
                    String series = exercise.optString("series", "—");
                    String repetitions = exercise.optString("repeticoes", "—");
                    String rest = exercise.optString("descanso", "—");

                    TextView item = createText(
                            name + "\n"
                                    + series + " séries · "
                                    + repetitions + " repetições\n"
                                    + "Descanso: " + rest,
                            false
                    );

                    item.setOnClickListener(v -> {
                        Bundle args = new Bundle();
                        args.putString("exercise_json", exercise.toString());

                        NavHostFragment.findNavController(this)
                                .navigate(
                                        R.id.action_treino_to_exercicio,
                                        args
                                );
                    });

                    exercisesContainer.addView(item);
                }

                finish.setEnabled(true);

                finish.setOnClickListener(v -> {
                    Toast.makeText(
                            requireContext(),
                            "Treino concluído!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish.setEnabled(false);
                    finish.setText("Treino concluído");
                });

            } catch (JSONException exception) {
                exercisesContainer.addView(createText(
                        "Não foi possível carregar os exercícios: "
                                + exception.getMessage(),
                        true
                ));

                finish.setEnabled(false);
            }
        }

        private JSONObject chooseWorkout(JSONArray days) {
            if (days == null || days.length() == 0) {
                return null;
            }

            int index = (int) (
                    (System.currentTimeMillis() / 86_400_000L)
                            % days.length()
            );

            JSONObject selected = days.optJSONObject(index);

            if (selected == null
                    || selected.optJSONArray("exercicios") == null) {
                return days.optJSONObject(0);
            }

            return selected;
        }

        private TextView createText(String text, boolean plain) {
            TextView item = new TextView(requireContext());

            item.setText(text);
            item.setTextColor(0xFFF4F5F2);
            item.setTextSize(plain ? 14 : 16);
            item.setPadding(16, 18, 16, 18);

            if (!plain) {
                item.setBackgroundColor(0xFF202720);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                params.bottomMargin = 8;
                item.setLayoutParams(params);
                item.setClickable(true);
                item.setFocusable(true);
            }

            return item;
        }
    }

