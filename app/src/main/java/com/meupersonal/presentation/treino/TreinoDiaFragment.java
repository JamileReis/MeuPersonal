package com.meupersonal.presentation.treino;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.List;


    public final class TreinoDiaFragment extends Fragment {

        private LocalWorkoutStore store;
        private LocalWorkoutStore.PlanRecord planRecord;
        private JSONArray workoutDays;
        private LinearLayout exercisesContainer;
        private TextView dayTitle;
        private TextView dayDescription;
        private Button startButton;
        private Button finishButton;
        private int selectedDayIndex;
        private long sessionId = -1L;

        public TreinoDiaFragment() {
            super(R.layout.fragment_treino_dia);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
            super.onViewCreated(view, state);

            TextView title = view.findViewById(R.id.textTituloTreino);
            TextView objective = view.findViewById(R.id.textObjetivoTreino);
            Spinner daySpinner = view.findViewById(R.id.spinnerDiasTreino);
            dayTitle = view.findViewById(R.id.textNomeDiaTreino);
            dayDescription = view.findViewById(R.id.textDescricaoTreino);
            exercisesContainer = view.findViewById(R.id.containerExercicios);
            Button alterButton = view.findViewById(R.id.btnAlterarTreinoIa);
            startButton = view.findViewById(R.id.btnIniciarTreino);
            finishButton = view.findViewById(R.id.btnFinalizarTreino);

            store = new LocalWorkoutStore(requireContext());
            Bundle arguments = getArguments();
            long requestedPlanId = arguments == null ? -1L : arguments.getLong("plan_id", -1L);
            planRecord = requestedPlanId >= 0 ? store.getPlan(requestedPlanId) : store.getLatestPlan();

            alterButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_treino_to_prompt));

            if (planRecord == null) {
                title.setText("Seu treino personalizado");
                objective.setText("Gere um plano para visualizar seus exercícios.");
                dayTitle.setText("Nenhum treino disponível");
                addMessage("Você ainda não possui um plano salvo.");
                startButton.setEnabled(false);
                finishButton.setEnabled(false);
                alterButton.setText("Criar meu treino com IA");
                return;
            }

            try {
                JSONObject plan = new JSONObject(planRecord.json);
                title.setText(planRecord.title);
                objective.setText(planRecord.objective);
                workoutDays = plan.optJSONArray("dias");

                if (workoutDays == null || workoutDays.length() == 0) {
                    throw new JSONException("O plano não possui dias de treinamento.");
                }

                List<String> dayLabels = new ArrayList<>();
                for (int i = 0; i < workoutDays.length(); i++) {
                    JSONObject day = workoutDays.optJSONObject(i);
                    String label = day == null ? "Treino " + (i + 1)
                            : day.optString("dia", "Dia " + (i + 1)) + " · "
                              + day.optString("nome", "Treino " + (i + 1));
                    dayLabels.add(label);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, dayLabels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                daySpinner.setAdapter(adapter);
                daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View item, int position, long id) {
                        selectedDayIndex = position;
                        sessionId = store.getOpenSession(planRecord.id, selectedDayIndex);
                        startButton.setEnabled(sessionId < 0);
                        finishButton.setEnabled(sessionId >= 0);
                        renderSelectedDay();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        exercisesContainer.removeAllViews();
                    }
                });

                startButton.setOnClickListener(v -> startWorkout());
                finishButton.setOnClickListener(v -> finishWorkout());
            } catch (JSONException exception) {
                addMessage("Não foi possível carregar o plano: " + exception.getMessage());
                startButton.setEnabled(false);
                finishButton.setEnabled(false);
            }
        }

        private void renderSelectedDay() {
            exercisesContainer.removeAllViews();
            JSONObject day = workoutDays == null ? null : workoutDays.optJSONObject(selectedDayIndex);
            if (day == null) {
                addMessage("Não foi possível localizar este dia do plano.");
                return;
            }

            dayTitle.setText(day.optString("nome", day.optString("dia", "Treino")));
            dayDescription.setText(day.optString("descricao", ""));

            JSONArray exercises = day.optJSONArray("exercicios");
            if (exercises == null || exercises.length() == 0) {
                addMessage("Este dia ainda não possui exercícios.");
                return;
            }

            for (int i = 0; i < exercises.length(); i++) {
                JSONObject exercise = exercises.optJSONObject(i);
                if (exercise == null) continue;

                String name = exercise.optString("nome", "Exercício");
                String series = exercise.optString("series", "—");
                String repetitions = exercise.optString("repeticoes", "—");
                int rest = exercise.optInt("descansoSegundos",
                        exercise.optInt("descanso", 0));
                String group = exercise.optString("grupoMuscular", "");

                LinearLayout card = new LinearLayout(requireContext());
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(18, 16, 18, 16);
                card.setBackgroundColor(0xFF202720);
                card.setClickable(true);
                card.setFocusable(true);

                TextView nameView = createText(name, 17, 0xFFF4F5F2, true);
                TextView groupView = createText(group, 13, 0xFFA8B0A8, false);
                TextView detailsView = createText(series + " séries · " + repetitions
                        + " repetições · " + rest + " s de descanso", 14, 0xFFB7F36B, false);
                card.addView(nameView);
                if (!group.isEmpty()) card.addView(groupView);
                card.addView(detailsView);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = dp(10);
                card.setLayoutParams(params);
                final int exerciseIndex = i;
                card.setOnClickListener(v -> openExercise(exercise, exerciseIndex));
                exercisesContainer.addView(card);
            }
        }

        private void openExercise(JSONObject exercise, int exerciseIndex) {
            Bundle args = new Bundle();
            args.putString("exercise_json", exercise.toString());
            args.putLong("plan_id", planRecord.id);
            args.putInt("day_index", selectedDayIndex);
            args.putInt("exercise_index", exerciseIndex);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_treino_to_exercicio, args);
        }

        private void startWorkout() {
            JSONObject day = workoutDays.optJSONObject(selectedDayIndex);
            if (day == null) return;
            sessionId = store.startSession(planRecord.id, selectedDayIndex,
                    day.optString("nome", "Treino"), System.currentTimeMillis());
            startButton.setEnabled(false);
            finishButton.setEnabled(true);
            Toast.makeText(requireContext(), "Sessão iniciada!", Toast.LENGTH_SHORT).show();
        }

        private void finishWorkout() {
            if (sessionId < 0) return;
            store.finishSession(sessionId, System.currentTimeMillis());
            sessionId = -1L;
            startButton.setEnabled(true);
            finishButton.setEnabled(false);
            Toast.makeText(requireContext(), "Treino concluído e salvo no histórico!",
                    Toast.LENGTH_LONG).show();
        }

        private void addMessage(String message) {
            exercisesContainer.addView(createText(message, 14, 0xFFA8B0A8, false));
        }

        private TextView createText(String value, float size, int color, boolean bold) {
            TextView text = new TextView(requireContext());
            text.setText(value);
            text.setTextColor(color);
            text.setTextSize(size);
            if (bold) text.setTypeface(null, android.graphics.Typeface.BOLD);
            return text;
        }

        private int dp(int value) {
            return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
        }
    }