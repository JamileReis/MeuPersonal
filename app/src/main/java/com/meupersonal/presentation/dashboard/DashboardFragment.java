package com.meupersonal.presentation.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.meupersonal.R;
import com.meupersonal.data.auth.SecureSessionStore;
import com.meupersonal.model.auth.AuthSession;

public final class DashboardFragment extends Fragment {
    public DashboardFragment() { super(R.layout.fragment_dashboard); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        TextView greeting = view.findViewById(R.id.textSaudacao);
        AuthSession session = new SecureSessionStore(requireContext()).read();
        if (session != null && !session.getEmail().isEmpty()) greeting.setText("Olá! " + session.getEmail());
        View card = view.findViewById(R.id.cardAcessoTreinoDia);
        card.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_dashboard_to_treino_dia));
    }
}
