package com.meupersonal.presentation.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.meupersonal.R;
import com.meupersonal.data.LocalWorkoutStore;
import com.meupersonal.data.auth.SecureSessionStore;
import com.meupersonal.model.auth.AuthSession;

public final class DashboardFragment extends Fragment {
    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle state
    ) {
        super.onViewCreated(view, state);

        TextView greeting = view.findViewById(R.id.textSaudacao);
        TextView progress = view.findViewById(R.id.textProgresso);
        View workoutCard = view.findViewById(R.id.cardAcessoTreinoDia);

        AuthSession session = new SecureSessionStore(requireContext()).read();

        if (session != null) {
            String email = session.getEmail();

            if (email != null && !email.isEmpty()) {
                String firstName = email.split("@")[0].split("[._-]")[0];
                greeting.setText("Olá, " + firstName + "!");
            }
        }

        LocalWorkoutStore store = new LocalWorkoutStore(requireContext());

        int completed = store.countCompletedSessionsLastDays(7);

        progress.setText(
                completed + (completed == 1
                        ? " treino concluído nos últimos 7 dias"
                        : " treinos concluídos nos últimos 7 dias")
        );

        workoutCard.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_dashboard_to_treino_dia)
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getView();

        if (view != null) {
            TextView progress = view.findViewById(R.id.textProgresso);

            try (LocalWorkoutStore store = new LocalWorkoutStore(requireContext())) {
                int completed = store.countCompletedSessionsLastDays(7);

                progress.setText(
                        completed + (completed == 1
                                ? " treino concluído nos últimos 7 dias"
                                : " treinos concluídos nos últimos 7 dias")
                );
            }
        }
    }
}