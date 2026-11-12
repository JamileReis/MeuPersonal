package com.meupersonal.presentation.auth;

public final class AuthUiState {
    public enum Type { SUCCESS, EMAIL_CONFIRMATION, ERROR }
    private final Type type;
    private final String message;

    public AuthUiState(Type type, String message) {
        this.type = type;
        this.message = message == null ? "" : message;
    }

    public Type getType() { return type; }
    public String getMessage() { return message; }
}
