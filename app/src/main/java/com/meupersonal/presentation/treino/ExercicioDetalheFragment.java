package com.meupersonal.presentation.treino;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.meupersonal.R;
import com.meupersonal.data.LocalWorkoutStore;
import com.meupersonal.service.ia.GeminiAiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ExercicioDetalheFragment extends Fragment {
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private ImageView image;
    private ProgressBar progress;
    private TextView imageStatus;

    public ExercicioDetalheFragment() {
        super(R.layout.fragment_exercicio_detalhe);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle state
    ) {
        super.onViewCreated(view, state);

        Button back = view.findViewById(R.id.btnVoltarTreino);

        image = view.findViewById(R.id.imageDetalheExercicio);
        progress = view.findViewById(R.id.progressImagemExercicio);
        imageStatus = view.findViewById(R.id.textImagemExercicio);

        back.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        Bundle args = getArguments();

        String raw = args == null
                ? null
                : args.getString("exercise_json");

        if (raw == null || raw.trim().isEmpty()) {
            show(view, R.id.textDetalheNome, "Exercício indisponível");
            imageStatus.setText("Não foi possível carregar o exercício.");
            progress.setVisibility(View.GONE);
            return;
        }

        try {
            JSONObject exercise = new JSONObject(raw);

            show(view, R.id.textDetalheNome,
                    exercise.optString("nome", "Exercício"));

            show(view, R.id.textDetalheGrupo,
                    exercise.optString(
                            "grupoMuscular",
                            exercise.optString("grupo", "Grupo muscular")
                    ));

            show(view, R.id.textDetalheSeries,
                    exercise.optString("series", "—") + " séries");

            show(view, R.id.textDetalheReps,
                    exercise.optString("repeticoes", "—") + " reps");

            int rest = exercise.optInt(
                    "descansoSegundos",
                    exercise.optInt("descanso", 0)
            );

            show(view, R.id.textDetalheDescanso,
                    rest > 0 ? rest + " s" : "Descanso —");

            show(view, R.id.textDetalheIntensidade,
                    exercise.optString(
                            "intensidade",
                            "Intensidade não informada"
                    ));

            show(view, R.id.textDetalheCarga,
                    buildLoadDescription(exercise));

            show(view, R.id.textDetalheInstrucao,
                    exercise.optString(
                            "instrucao",
                            exercise.optString(
                                    "instrucoes",
                                    "Execute o movimento de forma controlada."
                            )
                    ));

            show(view, R.id.textDetalheDescricao,
                    exercise.optString("descricao", ""));

            loadExerciseImage(args, exercise);

        } catch (JSONException exception) {
            show(view, R.id.textDetalheNome,
                    "Não foi possível abrir este exercício.");

            imageStatus.setText(
                    "Dados do exercício inválidos: " + exception.getMessage()
            );

            progress.setVisibility(View.GONE);
        }
    }

    private String buildLoadDescription(JSONObject exercise) {
        Object rawLoad = exercise.opt("carga");

        String loadType = exercise.optString(
                "tipoCarga",
                "PESO_LIVRE"
        );

        String loadNote = exercise.optString(
                "observacaoCarga",
                ""
        );

        if ("PESO_CORPORAL".equalsIgnoreCase(loadType)) {
            return "Carga sugerida: peso corporal"
                    + appendNote(loadNote);
        }

        if (!(rawLoad instanceof Number)) {
            return "Carga inicial: não calculada. "
                    + "É necessário calibrar a carga antes da execução.";
        }

        double load = ((Number) rawLoad).doubleValue();

        if (load < 0) {
            return "Carga sugerida inválida.";
        }

        String formattedLoad = load == Math.floor(load)
                ? String.format(java.util.Locale.getDefault(), "%.0f", load)
                : String.format(java.util.Locale.getDefault(), "%.1f", load);

        return "Carga inicial sugerida: " + formattedLoad + " kg"
                + appendNote(loadNote);
    }

    private String appendNote(String note) {
        return note == null || note.trim().isEmpty()
                ? ""
                : "\nProgressão: " + note.trim();
    }

    private void loadExerciseImage(
            Bundle args,
            JSONObject exercise
    ) {
        long planId = args.getLong("plan_id", -1L);
        int dayIndex = args.getInt("day_index", -1);
        int exerciseIndex = args.getInt("exercise_index", -1);

        String exerciseName = exercise.optString(
                "nome",
                "Exercício"
        );

        String group = exercise.optString(
                "grupoMuscular",
                ""
        );

        String description = exercise.optString(
                "descricao",
                ""
        );

        LocalWorkoutStore store =
                new LocalWorkoutStore(requireContext());

        executor.execute(() -> {
            try {
                byte[] cached = planId >= 0
                        && dayIndex >= 0
                        && exerciseIndex >= 0
                        ? store.getExerciseImage(
                        planId,
                        dayIndex,
                        exerciseIndex
                )
                        : null;

                if (cached != null && cached.length > 0) {
                    showImage(cached, "Demonstração do exercício");
                    return;
                }

                byte[] generated = new GeminiAiService()
                        .generateWorkoutImage(
                                exerciseName,
                                group + ". " + description
                        );

                if (generated == null || generated.length == 0) {
                    throw new IOException(
                            "O Gemini não retornou uma imagem."
                    );
                }

                if (planId >= 0
                        && dayIndex >= 0
                        && exerciseIndex >= 0) {
                    store.saveExerciseImage(
                            planId,
                            dayIndex,
                            exerciseIndex,
                            generated
                    );
                }

                showImage(
                        generated,
                        "Imagem demonstrativa gerada por IA"
                );

            } catch (IOException | RuntimeException exception) {
                String message = exception.getMessage();

                showImageError(
                        message == null || message.trim().isEmpty()
                                ? "Falha ao gerar a imagem."
                                : "Falha ao gerar imagem: " + message
                );
            }
        });
    }

    private void showImage(byte[] bytes, String label) {
        mainHandler.post(() -> {
            if (!isAdded() || image == null) {
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    bytes,
                    0,
                    bytes.length
            );

            if (bitmap == null) {
                showImageError("A API retornou dados que não formam uma imagem.");
                return;
            }

            image.setImageBitmap(bitmap);
            image.setVisibility(View.VISIBLE);
            imageStatus.setText(label);
            progress.setVisibility(View.GONE);
        });
    }

    private void showImageError(String message) {
        mainHandler.post(() -> {
            if (!isAdded() || imageStatus == null || progress == null) {
                return;
            }

            progress.setVisibility(View.GONE);
            imageStatus.setText(message);
        });
    }

    private void show(View root, int id, String value) {
        TextView text = root.findViewById(id);

        if (text != null) {
            text.setText(value);
        }
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}