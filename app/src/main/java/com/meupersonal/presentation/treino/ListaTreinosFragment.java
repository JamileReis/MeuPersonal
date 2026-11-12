package com.meupersonal.presentation.treino;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.meupersonal.R;

public final class ListaTreinosFragment extends Fragment {
    public ListaTreinosFragment() { super(R.layout.fragment_lista_treinos); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        Button create = view.findViewById(R.id.btnNovoTreino);
        create.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_lista_to_treino_dia));
    }
}
