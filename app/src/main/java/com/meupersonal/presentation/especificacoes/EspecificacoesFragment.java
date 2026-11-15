package com.meupersonal.presentation.especificacoes;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.meupersonal.R;
import com.meupersonal.data.auth.SecureSessionStore;
import com.meupersonal.model.auth.AuthSession;
import com.meupersonal.presentation.auth.AuthViewModel;

public final class EspecificacoesFragment extends Fragment {
    public EspecificacoesFragment() { super(R.layout.fragment_especificacoes); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LinearLayout content = (LinearLayout) ((android.widget.ScrollView) view).getChildAt(0);
        TextView account = new TextView(requireContext());
        AuthSession session = new SecureSessionStore(requireContext()).read();
        account.setText(session == null ? "Conta" : "Conectado como " + session.getEmail());
        account.setTextColor(android.graphics.Color.WHITE);
        account.setPadding(0, 32, 0, 20);
        content.addView(account);

        Button logout = new Button(requireContext());
        logout.setText("Sair da conta");
        content.addView(logout);
        AuthViewModel viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        logout.setOnClickListener(v -> {
            logout.setEnabled(false);
            viewModel.logout();
            NavHostFragment.findNavController(this).navigate(R.id.action_global_login);
        });
    }
}
