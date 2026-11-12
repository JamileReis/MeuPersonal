package com.meupersonal.presentation.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.meupersonal.data.auth.SecureSessionStore;
import com.meupersonal.model.auth.AuthSession;
import com.meupersonal.repository.auth.AuthRepository;
import com.meupersonal.service.auth.SupabaseAuthService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AuthViewModel extends AndroidViewModel {
    private final AuthRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<AuthUiState> authState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sessionRestored = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(
                new SupabaseAuthService(),
                new SecureSessionStore(application)
        );
    }

    public LiveData<AuthUiState> getAuthState() { return authState; }
    public LiveData<Boolean> getSessionRestored() { return sessionRestored; }

    public void restoreSession() {
        executor.execute(() -> sessionRestored.postValue(repository.restoreSession()));
    }

    public void login(String email, String password) {
        executor.execute(() -> {
            try {
                repository.login(email, password);
                authState.postValue(new AuthUiState(AuthUiState.Type.SUCCESS, "Login realizado."));
            } catch (Exception error) {
                authState.postValue(new AuthUiState(AuthUiState.Type.ERROR, friendlyMessage(error)));
            }
        });
    }

    public void register(String name, String email, String password) {
        executor.execute(() -> {
            try {
                AuthSession session = repository.register(name, email, password);
                if (session == null) {
                    authState.postValue(new AuthUiState(
                            AuthUiState.Type.EMAIL_CONFIRMATION,
                            "Cadastro realizado. Verifique seu e-mail para confirmar a conta antes de entrar."
                    ));
                } else {
                    authState.postValue(new AuthUiState(AuthUiState.Type.SUCCESS, "Conta criada com sucesso."));
                }
            } catch (Exception error) {
                authState.postValue(new AuthUiState(AuthUiState.Type.ERROR, friendlyMessage(error)));
            }
        });
    }

    public void logout() {
        executor.execute(repository::logout);
    }

    public void clearAuthState() {
        authState.setValue(null);
    }

    private String friendlyMessage(Throwable error) {
        String message = error == null ? null : error.getMessage();
        if (message == null || message.trim().isEmpty()) return "Não foi possível concluir. Tente novamente.";
        String lower = message.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("invalid login credentials")) return "E-mail ou senha incorretos.";
        if (lower.contains("user already registered")) return "Este e-mail já possui uma conta.";
        if (lower.contains("email not confirmed")) return "Confirme seu e-mail antes de entrar.";
        if (lower.contains("password should be at least")) return "A senha não atende ao tamanho mínimo configurado.";
        if (lower.contains("unable to validate email")) return "Informe um endereço de e-mail válido.";
        return message;
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
        super.onCleared();
    }
}
