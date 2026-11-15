package com.meupersonal.model.auth;

import org.json.JSONException;
import org.json.JSONObject;

public final class AuthSession {
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final String email;
    private final String userId;
    private final long expiresAtMillis;

    public AuthSession(String accessToken, String refreshToken, String tokenType,
                       String email, String userId, long expiresAtMillis) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType == null || tokenType.isEmpty() ? "bearer" : tokenType;
        this.email = email == null ? "" : email;
        this.userId = userId == null ? "" : userId;
        this.expiresAtMillis = expiresAtMillis;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public String getEmail() { return email; }
    public String getUserId() { return userId; }
    public long getExpiresAtMillis() { return expiresAtMillis; }
    public boolean isExpiring() { return expiresAtMillis <= System.currentTimeMillis() + 60_000L; }

    public JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("access_token", accessToken)
                .put("refresh_token", refreshToken)
                .put("token_type", tokenType)
                .put("email", email)
                .put("user_id", userId)
                .put("expires_at", expiresAtMillis);
    }

    public static AuthSession fromJson(JSONObject json) {
        return new AuthSession(
                json.optString("access_token", ""),
                json.optString("refresh_token", ""),
                json.optString("token_type", "bearer"),
                json.optString("email", ""),
                json.optString("user_id", ""),
                json.optLong("expires_at", 0L)
        );
    }
}
