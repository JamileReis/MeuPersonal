package com.meupersonal.presentation.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.meupersonal.R;
import com.meupersonal.presentation.auth.AuthUiState;
import com.meupersonal.presentation.auth.AuthViewModel;

public final class RedefinirSenhaFragment extends Fragment {
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button saveButton;
    private ProgressBar progress;
    private AuthViewModel viewModel;
    private String recoveryAccessToken;

    public RedefinirSenhaFragment() {
        super(R.layout.fragment_redefinir_senha);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        passwordInput = view.findViewById(R.id.inputNovaSenha);
        confirmPasswordInput = view.findViewById(R.id.inputConfirmarNovaSenha);
        saveButton = view.findViewById(R.id.btnSalvarNovaSenha);
        progress = view.findViewById(R.id.progressNovaSenha);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        Bundle args = getArguments();
        recoveryAccessToken = args == null ? null : args.getString("recovery_access_token");
        if (recoveryAccessToken == null || recoveryAccessToken.trim().isEmpty()) {
            saveButton.setEnabled(false);
            Toast.makeText(requireContext(),
                    "O link não contém uma autorização válida. Solicite outro e-mail de recuperação.",
                    Toast.LENGTH_LONG).show();
        }

        saveButton.setOnClickListener(v -> submitNewPassword());
        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            setLoading(false);
            viewModel.clearAuthState();
            if (state.getType() == AuthUiState.Type.PASSWORD_UPDATED) {
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).navigate(R.id.action_global_login);
            } else if (state.getType() == AuthUiState.Type.ERROR) {
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void submitNewPassword() {
        String password = passwordInput.getText().toString();
        String confirmation = confirmPasswordInput.getText().toString();
        if (password.length() < 8) {
            passwordInput.setError("A senha deve ter pelo menos 8 caracteres.");
            passwordInput.requestFocus();
            return;
        }
        if (!password.equals(confirmation)) {
            confirmPasswordInput.setError("As senhas não coincidem.");
            confirmPasswordInput.requestFocus();
            return;
        }
        setLoading(true);
        viewModel.updatePassword(recoveryAccessToken, password);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading && recoveryAccessToken != null
                && !recoveryAccessToken.trim().isEmpty());
        passwordInput.setEnabled(!loading);
        confirmPasswordInput.setEnabled(!loading);
    }
}
