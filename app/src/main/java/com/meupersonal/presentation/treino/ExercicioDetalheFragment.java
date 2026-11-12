package com.meupersonal.presentation.treino;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import androidx.navigation.fragment.NavHostFragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.meupersonal.R;
import org.json.JSONException;
import org.json.JSONObject;
public final class ExercicioDetalheFragment extends Fragment {
    public ExercicioDetalheFragment() { super(R.layout.fragment_exercicio_detalhe); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        Button back = view.findViewById(R.id.btnVoltarTreino);
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        Bundle args = getArguments();
        String raw = args == null ? null : args.getString("exercise_json");
        if (raw == null) {
            show(view, "textDetalheNome", "Exercício indisponível");
            return;
        }
        try {
            JSONObject exercise = new JSONObject(raw);
            show(view, "textDetalheNome", exercise.optString("nome", "Exercício"));
            show(view, "textDetalheGrupo", exercise.optString("grupoMuscular",
                    exercise.optString("grupo", "Grupo muscular")));
            show(view, "textDetalheSeries", exercise.optInt("series") + " séries");
            show(view, "textDetalheReps", exercise.optString("repeticoes", "—") + " reps");
            show(view, "textDetalheDescanso", exercise.optInt("descansoSegundos") + " s");
            show(view, "textDetalheIntensidade", exercise.optString("intensidade", ""));
            Object load = exercise.opt("carga");
            show(view, "textDetalheCarga", load == null || load == JSONObject.NULL
                    ? "Carga: conforme orientação profissional" : "Carga: " + load + " kg");
            show(view, "textDetalheInstrucao", exercise.optString("instrucao",
                    exercise.optString("instrucoes", "Siga a execução orientada pelo profissional.")));
            show(view, "textDetalheDescricao", exercise.optString("descricao", ""));
        } catch (JSONException exception) {
            show(view, "textDetalheNome", "Não foi possível abrir este exercício.");
        }
    }

    private void show(View root, String resourceName, String value) {
        int id = getResources().getIdentifier(resourceName, "id", requireContext().getPackageName());
        TextView text = root.findViewById(id);
        if (text != null) text.setText(value);
    }
}
