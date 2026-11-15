package com.meupersonal.service.auth;

import java.io.IOException;

public final class SupabaseAuthException extends IOException {
    private final int statusCode;

    public SupabaseAuthException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
