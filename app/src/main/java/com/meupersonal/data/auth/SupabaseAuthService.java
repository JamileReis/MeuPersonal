package com.meupersonal.data.auth;

import com.meupersonal.model.auth.AuthSession;
import com.meupersonal.service.auth.SupabaseAuthException;
import com.meupersonal.service.treino.SupabaseClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class SupabaseAuthService {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public AuthSession login(String email, String password) throws IOException {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException error) {
            throw new IOException("Não foi possível preparar a autenticação.", error);
        }
        return executeSessionRequest("/auth/v1/token?grant_type=password", body);
    }

    public AuthSession register(String name, String email, String password) throws IOException {
        JSONObject metadata = new JSONObject();
        JSONObject body = new JSONObject();
        try {
            metadata.put("full_name", name);
            body.put("email", email);
            body.put("password", password);
            body.put("data", metadata);
        } catch (JSONException error) {
            throw new IOException("Não foi possível preparar o cadastro.", error);
        }
        return executeSessionRequest("/auth/v1/signup", body);
    }

    public AuthSession refresh(String refreshToken) throws IOException {
        JSONObject body = new JSONObject();
        try {
            body.put("refresh_token", refreshToken);
        } catch (JSONException error) {
            throw new IOException("Não foi possível renovar a sessão.", error);
        }
        return executeSessionRequest("/auth/v1/token?grant_type=refresh_token", body);
    }

    public void logout(String accessToken) throws IOException {
        Request request = SupabaseClient.novaRequisicao("/auth/v1/logout")
                .header("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(JSON, "{}"))
                .build();
        try (Response response = SupabaseClient.getCliente().newCall(request).execute()) {
            if (!response.isSuccessful() && response.code() != 401) {
                throw new IOException(readError(response));
            }
        }
    }

    private AuthSession executeSessionRequest(String path, JSONObject payload) throws IOException {
        Request request;
        try {
            request = SupabaseClient.novaRequisicao(path)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(JSON, payload.toString()))
                    .build();
        } catch (IllegalStateException error) {
            throw new IOException(error.getMessage(), error);
        }

        try (Response response = SupabaseClient.getCliente().newCall(request).execute()) {
            String responseBody = readBody(response);
            if (!response.isSuccessful()) {
                throw new SupabaseAuthException(response.code(), parseError(responseBody, response.code()));
            }
            if (responseBody.isEmpty()) return null;
            try {
                JSONObject json = new JSONObject(responseBody);
                String accessToken = json.optString("access_token", "");
                String refreshToken = json.optString("refresh_token", "");
                if (accessToken.isEmpty() || refreshToken.isEmpty()) return null;

                JSONObject user = json.optJSONObject("user");
                if (user == null) user = new JSONObject();
                long expiresAt = json.optLong("expires_at", 0L) * 1000L;
                if (expiresAt <= 0L) {
                    expiresAt = System.currentTimeMillis() + json.optLong("expires_in", 3600L) * 1000L;
                }
                return new AuthSession(accessToken, refreshToken,
                        json.optString("token_type", "bearer"),
                        user.optString("email", ""), user.optString("id", ""), expiresAt);
            } catch (JSONException error) {
                throw new IOException("O Supabase retornou uma resposta inválida.", error);
            }
        }
    }

    private String readBody(Response response) throws IOException {
        ResponseBody body = response.body();
        return body == null ? "" : body.string();
    }

    private String readError(Response response) throws IOException {
        return parseError(readBody(response), response.code());
    }

    private String parseError(String body, int status) {
        try {
            JSONObject json = new JSONObject(body);
            String message = json.optString("msg", "");
            if (message.isEmpty()) message = json.optString("message", "");
            if (message.isEmpty()) message = json.optString("error_description", "");
            if (message.isEmpty()) message = json.optString("error", "");
            if (!message.isEmpty()) return message;
        } catch (JSONException ignored) {
        }
        return "Falha na autenticação (HTTP " + status + "). Verifique a conexão e tente novamente.";
    }
    public void recoverPassword(String email) throws IOException {
        validateEmail(email);

        JSONObject payload = new JSONObject();

        try {
            payload.put("email", email.trim());
        } catch (JSONException error) {
            throw new IOException("Falha ao preparar a recuperação de senha.", error);
        }

        executeActionRequest("/auth/v1/recover", payload);
    }

    public void resendSignupConfirmation(String email) throws IOException {
        validateEmail(email);

        JSONObject payload = new JSONObject();

        try {
            payload.put("type", "signup");
            payload.put("email", email.trim());
        } catch (JSONException error) {
            throw new IOException("Falha ao preparar a confirmação.", error);
        }

        executeActionRequest("/auth/v1/resend", payload);
    }

    private void executeActionRequest(String path, JSONObject payload) throws IOException {
        Request request;

        try {
            request = SupabaseClient.novaRequisicao(path)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(JSON, payload.toString()))
                    .build();
        } catch (IllegalStateException error) {
            throw new IOException(error.getMessage(), error);
        }

        try (Response response = SupabaseClient.getCliente().newCall(request).execute()) {
            String responseBody = readBody(response);

            if (!response.isSuccessful()) {
                throw new SupabaseAuthException(
                        response.code(),
                        parseError(responseBody, response.code())
                );
            }
        }
    }

    private void validateEmail(String email) throws IOException {
        if (email == null || !android.util.Patterns.EMAIL_ADDRESS
                .matcher(email.trim()).matches()) {
            throw new IOException("Informe um endereço de e-mail válido.");
        }
    }
}
