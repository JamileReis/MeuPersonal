package com.meupersonal.repository.auth;
import com.meupersonal.data.auth.SecureSessionStore;
import com.meupersonal.model.auth.AuthSession;
import com.meupersonal.service.auth.SupabaseAuthService;

public final class AuthRepository {
    private final SupabaseAuthService authService;
    private final SecureSessionStore sessionStore;

    public AuthRepository(
            SupabaseAuthService authService,
            SecureSessionStore sessionStore
    ) {
        this.authService = authService;
        this.sessionStore = sessionStore;
    }

    public void login(String email, String password) throws Exception {
        AuthSession session = authService.login(email, password);

        if (session == null) {
            throw new IllegalStateException(
                    "O servidor não retornou uma sessão válida."
            );
        }

        sessionStore.save(session);
    }

    public AuthSession register(
            String name,
            String email,
            String password
    ) throws Exception {

        AuthSession session = authService.register(name, email, password);

        if (session != null) {
            sessionStore.save(session);
        }

        return session;
    }

    public boolean restoreSession() {
        AuthSession session = sessionStore.read();

        if (session == null) {
            return false;
        }

        if (!session.isExpiring()) {
            return true;
        }

        try {
            AuthSession refreshed = authService.refresh(
                    session.getRefreshToken()
            );

            if (refreshed == null) {
                return false;
            }

            sessionStore.save(refreshed);
            return true;

        } catch (Exception error) {
            return false;
        }
    }

    public void logout() {
        AuthSession session = sessionStore.read();

        try {
            if (session != null) {
                authService.logout(session.getAccessToken());
            }
        } catch (Exception ignored) {
        } finally {
            sessionStore.clear();
        }
    }
}