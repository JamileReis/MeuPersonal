package com.meupersonal.service;

import com.meupersonal.model.auth.AuthSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class SupabaseDataService {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public void upsertProfile(AuthSession session, JSONObject profile) throws IOException {
        requireSession(session);
        try {
            JSONObject payload = new JSONObject(profile.toString())
                    .put("user_id", session.getUserId());
            send("/rest/v1/profiles?on_conflict=user_id", session, payload,
                    "POST", "resolution=merge-duplicates,return=minimal");
        } catch (JSONException error) {
            throw new IOException("Não foi possível preparar o perfil para sincronização.", error);
        }
    }

    public String createPlan(AuthSession session, String title, String objective,
                             String planJson, long createdAt) throws IOException {
        return syncPlan(session, UUID.randomUUID().toString(), title, objective, planJson, createdAt);
    }

    public String syncPlan(AuthSession session, String clientId, String title, String objective,
                           String planJson, long createdAt) throws IOException {
        requireSession(session);
        if (clientId == null || !clientId.matches("[0-9a-fA-F-]{36}")) {
            throw new IOException("Identificador do plano inválido para sincronização.");
        }
        try {
            JSONObject payload = new JSONObject()
                    .put("user_id", session.getUserId())
                    .put("client_id", clientId)
                    .put("title", title == null ? "Meu treino" : title)
                    .put("objective", objective == null ? "" : objective)
                    .put("plan_json", new JSONObject(planJson))
                    .put("created_at", new Date(createdAt).toInstant().toString());
            send("/rest/v1/workout_plans?on_conflict=user_id,client_id", session, payload,
                    "POST", "resolution=merge-duplicates,return=minimal");
            return clientId;
        } catch (JSONException error) {
            throw new IOException("Não foi possível preparar o plano para sincronização.", error);
        }
    }

    public void deletePlan(AuthSession session, String clientId) throws IOException {
        requireSession(session);
        if (clientId == null || !clientId.matches("[0-9a-fA-F-]{36}")) {
            throw new IOException("Identificador do plano inválido para exclusão remota.");
        }
        send("/rest/v1/workout_plans?client_id=eq." + clientId, session, null,
                "DELETE", "return=minimal");
    }

    public void createSession(AuthSession session, String clientPlanId, String title,
                              long startedAt, Long finishedAt, boolean completed) throws IOException {
        requireSession(session);
        try {
            JSONObject payload = new JSONObject()
                    .put("user_id", session.getUserId())
                    .put("client_plan_id", clientPlanId)
                    .put("title", title == null ? "Treino" : title)
                    .put("started_at", new Date(startedAt).toInstant().toString())
                    .put("completed", completed);
            if (finishedAt != null) {
                payload.put("finished_at", new Date(finishedAt).toInstant().toString());
            }
            send("/rest/v1/workout_sessions", session, payload, "POST", "return=minimal");
        } catch (JSONException error) {
            throw new IOException("Não foi possível preparar o histórico para sincronização.", error);
        }
    }

    private void send(String path, AuthSession session, JSONObject payload,
                      String method, String prefer) throws IOException {
        Request.Builder builder = SupabaseClient.novaRequisicao(path, session.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Prefer", prefer);
        if ("POST".equals(method)) {
            builder.post(RequestBody.create(JSON, payload == null ? "{}" : payload.toString()));
        } else if ("DELETE".equals(method)) {
            builder.delete();
        } else {
            throw new IOException("Método HTTP não suportado.");
        }
        try (Response response = SupabaseClient.getCliente().newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() == null ? "" : response.body().string();
                throw new IOException("Falha ao sincronizar dados com o Supabase (HTTP "
                        + response.code() + "): " + body);
            }
        }
    }

    private void requireSession(AuthSession session) throws IOException {
        if (session == null || isBlank(session.getAccessToken()) || isBlank(session.getUserId())) {
            throw new IOException("Não há uma sessão autenticada para sincronizar os dados.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
