package com.meupersonal.presentation.login;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.meupersonal.R;
import com.meupersonal.presentation.auth.AuthUiState;
import com.meupersonal.presentation.auth.AuthViewModel;

public final class LoginFragment extends Fragment {
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private ProgressBar progress;
    private AuthViewModel viewModel;

    public LoginFragment() { super(R.layout.fragment_login); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        emailInput = view.findViewById(R.id.editEmail);
        passwordInput = view.findViewById(R.id.editSenha);
        loginButton = view.findViewById(R.id.btnLogin);
        progress = view.findViewById(R.id.progressLogin);
        TextView registerLink = view.findViewById(R.id.btnAlternarModo);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        loginButton.setOnClickListener(v -> submitLogin());
        registerLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));

        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            setLoading(false);
            if (state.getType() == AuthUiState.Type.SUCCESS) {
                viewModel.clearAuthState();
                NavHostFragment.findNavController(this).navigate(R.id.action_global_dashboard);
            } else {
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_LONG).show();
                viewModel.clearAuthState();
            }
        });
    }

    private void submitLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Informe um e-mail válido.");
            emailInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Informe sua senha.");
            passwordInput.requestFocus();
            return;
        }
        setLoading(true);
        viewModel.login(email, password);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
    }

}
