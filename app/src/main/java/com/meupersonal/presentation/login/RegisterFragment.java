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

public final class RegisterFragment extends Fragment {
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private Button registerButton;
    private ProgressBar progress;
    private AuthViewModel viewModel;

    public RegisterFragment() { super(R.layout.activity_register); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nameInput = view.findViewById(R.id.editNomeCadastro);
        emailInput = view.findViewById(R.id.editEmailCadastro);
        passwordInput = view.findViewById(R.id.editSenhaCadastro);
        registerButton = view.findViewById(R.id.btnCriarConta);
        progress = view.findViewById(R.id.progressCadastro);
        TextView loginLink = view.findViewById(R.id.linkIrLogin);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        registerButton.setOnClickListener(v -> submitRegistration());
        loginLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_register_to_login));

        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            setLoading(false);
            viewModel.clearAuthState();
            if (state.getType() == AuthUiState.Type.SUCCESS) {
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).navigate(R.id.action_global_dashboard);
            } else if (state.getType() == AuthUiState.Type.EMAIL_CONFIRMATION) {
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).navigate(R.id.action_register_to_login);
            } else {
                Toast.makeText(requireContext(), state.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void submitRegistration() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (name.length() < 2) {
            nameInput.setError("Informe seu nome.");
            nameInput.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Informe um e-mail válido.");
            emailInput.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError("A senha deve ter pelo menos 6 caracteres.");
            passwordInput.requestFocus();
            return;
        }
        setLoading(true);
        viewModel.register(name, email, password);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
        nameInput.setEnabled(!loading);
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
    }
}
