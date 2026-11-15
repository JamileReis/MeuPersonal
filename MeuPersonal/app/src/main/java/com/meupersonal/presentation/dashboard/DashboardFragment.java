package com.meupersonal.presentation.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.meupersonal.R;
import com.meupersonal.presentation.dashboard.DashboardViewModel;

public final class DashboardFragment extends Fragment {
    private TextView progress;
    private TextView greeting;
    private TextView planTitle;
    private TextView planDescription;
    private DashboardViewModel viewModel;

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle state
    ) {
        super.onViewCreated(view, state);

        greeting = view.findViewById(R.id.textSaudacao);
        progress = view.findViewById(R.id.textConsistencia);
        planTitle = view.findViewById(R.id.textTituloPlano);
        planDescription = view.findViewById(R.id.textDescricaoPlano);
        View workoutCard = view.findViewById(R.id.cardAcessoTreinoDia);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        viewModel.getState().observe(getViewLifecycleOwner(), dashboardState -> {
            if (dashboardState == null) return;

            String nome = dashboardState.nome == null || dashboardState.nome.trim().isEmpty()
                    ? "Vamos treinar?"
                    : dashboardState.nome.trim().split(" ")[0];

            greeting.setText("Olá, " + nome);
            planTitle.setText(dashboardState.tituloPlano);
            planDescription.setText(dashboardState.descricaoPlano);

            progress.setText(
                    dashboardState.treinosConcluidos
                            + " treinos concluídos nos últimos 7 dias"
            );
        });

        workoutCard.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_dashboard_to_treino_dia)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.carregarDashboard();
    }

    @Override
    public void onDestroyView() {
        progress = null;
        greeting = null;
        planTitle = null;
        planDescription = null;
        super.onDestroyView();
    }
}