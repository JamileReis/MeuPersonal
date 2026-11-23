package com.meupersonal.presentation.perfil;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.meupersonal.R;
import com.meupersonal.data.auth.SecureSessionStore;
import com.meupersonal.model.auth.AuthSession;
import com.meupersonal.presentation.auth.AuthViewModel;
import com.meupersonal.service.SupabaseDataService;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PerfilFragment extends Fragment {

    private PerfilViewModel viewModel;
    private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor();

    private EditText nome;
    private EditText idade;
    private EditText peso;
    private EditText altura;
    private EditText objetivo;
    private EditText limitacoes;
    private EditText preferencias;

    public PerfilFragment() {
        super(R.layout.fragment_perfil);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        nome = view.findViewById(R.id.inputNome);
        idade = view.findViewById(R.id.inputIdade);
        peso = view.findViewById(R.id.inputPeso);
        altura = view.findViewById(R.id.inputAltura);
        objetivo = view.findViewById(R.id.inputObjetivo);
        limitacoes = view.findViewById(R.id.inputLimitacoes);
        preferencias = view.findViewById(R.id.inputPreferencias);

        Button salvar = view.findViewById(R.id.btnSalvarPerfil);
        Button sair = view.findViewById(R.id.btnSairConta);
        TextView emailConta = view.findViewById(R.id.textEmailConta);

        AuthSession session = new SecureSessionStore(requireContext()).read();
        emailConta.setText(session == null || session.getEmail() == null
                ? "Conta não identificada" : session.getEmail());

        AuthViewModel authViewModel = new ViewModelProvider(requireActivity())
                .get(AuthViewModel.class);
        sair.setOnClickListener(v -> {
            sair.setEnabled(false);
            authViewModel.logout();
            NavHostFragment.findNavController(this).navigate(R.id.action_global_login);
        });

        viewModel.getPerfil().observe(getViewLifecycleOwner(), perfil -> {
            if (perfil == null) return;

            nome.setText(perfil.nome);
            idade.setText(perfil.idade > 0 ? String.valueOf(perfil.idade) : "");
            peso.setText(perfil.peso);
            altura.setText(perfil.altura);
            objetivo.setText(perfil.objetivo);
            limitacoes.setText(perfil.limitacoes);
            preferencias.setText(perfil.preferencias);
        });

        salvar.setOnClickListener(v -> salvarPerfil());
    }

    private void salvarPerfil() {
        String nomeTexto = nome.getText().toString().trim();
        String idadeTexto = idade.getText().toString().trim();

        if (nomeTexto.isEmpty()) {
            nome.setError("Informe seu nome");
            nome.requestFocus();
            return;
        }

        int idadeValor = 0;

        if (!idadeTexto.isEmpty()) {
            try {
                idadeValor = Integer.parseInt(idadeTexto);
                if (idadeValor <= 0 || idadeValor > 120) {
                    idade.setError("Informe uma idade válida");
                    return;
                }
            } catch (NumberFormatException exception) {
                idade.setError("Informe uma idade válida");
                return;
            }
        }

        PerfilViewModel.Perfil perfil = new PerfilViewModel.Perfil(
                nomeTexto,
                idadeValor,
                peso.getText().toString().trim(),
                altura.getText().toString().trim(),
                objetivo.getText().toString().trim(),
                limitacoes.getText().toString().trim(),
                preferencias.getText().toString().trim()
        );

        if (viewModel.salvarPerfil(perfil)) {
            sincronizarPerfil(perfil);
            Toast.makeText(
                    requireContext(),
                    "Perfil salvo com sucesso",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    requireContext(),
                    "Não foi possível salvar o perfil",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void sincronizarPerfil(PerfilViewModel.Perfil perfil) {
        AuthSession session = new SecureSessionStore(requireContext()).read();
        if (session == null) return;
        syncExecutor.execute(() -> {
            try {
                JSONObject payload = new JSONObject()
                        .put("full_name", perfil.nome)
                        .put("age", perfil.idade > 0 ? perfil.idade : JSONObject.NULL)
                        .put("weight_kg", numberOrNull(perfil.peso))
                        .put("height_cm", numberOrNull(perfil.altura))
                        .put("objective", perfil.objetivo)
                        .put("limitations", perfil.limitacoes)
                        .put("preferences", perfil.preferencias);
                new SupabaseDataService().upsertProfile(session, payload);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(
                            requireContext(),
                            "Perfil sincronizado com o Supabase",
                            Toast.LENGTH_SHORT
                    ).show());
                }
            } catch (Exception error) {
                android.util.Log.w("MeuPersonal", "Perfil salvo localmente, mas não sincronizado.", error);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(
                            requireContext(),
                            "Perfil salvo localmente, mas não sincronizado com o Supabase",
                            Toast.LENGTH_LONG
                    ).show());
                }
            }
        });
    }

    private Object numberOrNull(String value) {
        if (value == null || value.trim().isEmpty()) return JSONObject.NULL;
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException error) {
            return JSONObject.NULL;
        }
    }

    @Override
    public void onDestroy() {
        syncExecutor.shutdownNow();
        super.onDestroy();
    }
}
