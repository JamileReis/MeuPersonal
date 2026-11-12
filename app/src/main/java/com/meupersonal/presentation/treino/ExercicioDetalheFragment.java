package com.meupersonal.presentation.treino;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import androidx.navigation.fragment.NavHostFragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.meupersonal.R;
import com.meupersonal.data.LocalWorkoutStore;
import com.meupersonal.service.ia.GeminiAiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ExercicioDetalheFragment extends Fragment {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ImageView image;
    private ProgressBar progress;
    private TextView imageStatus;

    public ExercicioDetalheFragment() {
        super(R.layout.fragment_exercicio_detalhe);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);

        Button back = view.findViewById(R.id.btnVoltarTreino);
        image = view.findViewById(R.id.imageDetalheExercicio);
        progress = view.findViewById(R.id.progressImagemExercicio);
        imageStatus = view.findViewById(R.id.textImagemExercicio);
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        Bundle args = getArguments();
        String raw = args == null ? null : args.getString("exercise_json");
        if (raw == null) {
            show(view, R.id.textDetalheNome, "Exercício indisponível");
            imageStatus.setText("Não foi possível carregar o exercício.");
            progress.setVisibility(View.GONE);
            return;
        }

        try {
            JSONObject exercise = new JSONObject(raw);
            show(view, R.id.textDetalheNome, exercise.optString("nome", "Exercício"));
            show(view, R.id.textDetalheGrupo, exercise.optString("grupoMuscular",
                    exercise.optString("grupo", "Grupo muscular")));
            show(view, R.id.textDetalheSeries, exercise.optString("series", "—") + " séries");
            show(view, R.id.textDetalheReps, exercise.optString("repeticoes", "—") + " reps");
            int rest = exercise.optInt("descansoSegundos", exercise.optInt("descanso", 0));
            show(view, R.id.textDetalheDescanso, rest > 0 ? rest + " s" : "Descanso —");
            show(view, R.id.textDetalheIntensidade,
                    exercise.optString("intensidade", "Intensidade não informada"));

            Object load = exercise.opt("carga");
            show(view, R.id.textDetalheCarga, load == null || load == JSONObject.NULL
                    ? "Carga: conforme orientação profissional" : "Carga: " + load + " kg");
            show(view, R.id.textDetalheInstrucao, exercise.optString("instrucao",
                    exercise.optString("instrucoes", "Siga a execução orientada pelo profissional.")));
            show(view, R.id.textDetalheDescricao, exercise.optString("descricao", ""));

            loadExerciseImage(args, exercise);
        } catch (JSONException exception) {
            show(view, R.id.textDetalheNome, "Não foi possível abrir este exercício.");
            imageStatus.setText("Dados do exercício inválidos.");
            progress.setVisibility(View.GONE);
        }
    }

    private void loadExerciseImage(Bundle args, JSONObject exercise) {
        long planId = args.getLong("plan_id", -1L);
        int dayIndex = args.getInt("day_index", -1);
        int exerciseIndex = args.getInt("exercise_index", -1);
        String exerciseName = exercise.optString("nome", "Exercício");
        LocalWorkoutStore store = new LocalWorkoutStore(requireContext());

        executor.execute(() -> {
            byte[] cached = planId >= 0 && dayIndex >= 0 && exerciseIndex >= 0
                    ? store.getExerciseImage(planId, dayIndex, exerciseIndex) : null;
            if (cached != null) {
                showImage(cached, "Demonstração do exercício");
                return;
            }

            try {
                byte[] generated = new GeminiAiService()
                        .generateWorkoutImage(exerciseName, exerciseName);
                if (planId >= 0 && dayIndex >= 0 && exerciseIndex >= 0) {
                    store.saveExerciseImage(planId, dayIndex, exerciseIndex, generated);
                }
                showImage(generated, "Imagem demonstrativa gerada por IA");
            } catch (IOException | RuntimeException exception) {
                showImageError("Não foi possível gerar a imagem. "
                        + "Confira a conexão e tente novamente.");
            }
        });
    }

    private void showImage(byte[] bytes, String label) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) return;
            android.graphics.Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                showImageError("A imagem recebida não é válida.");
                return;
            }
            image.setImageBitmap(bitmap);
            image.setVisibility(View.VISIBLE);
            imageStatus.setText(label);
            progress.setVisibility(View.GONE);
        });
    }

    private void showImageError(String message) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            imageStatus.setText(message);
        });
    }

    private void show(View root, int id, String value) {
        TextView text = root.findViewById(id);
        if (text != null) text.setText(value);
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}