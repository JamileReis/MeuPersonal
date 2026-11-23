package com.meupersonal.service.treino;

import com.meupersonal.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public final class SupabaseClient {
    private static final OkHttpClient CLIENT =
            new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

    private SupabaseClient() {
    }

    public static OkHttpClient getCliente() {
        return CLIENT;
    }

    public static Request.Builder novaRequisicao(String path) {
        return criarRequisicao(path, null);
    }

    public static Request.Builder novaRequisicao(
            String path,
            String accessToken
    ) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Token de autenticação não informado."
            );
        }

        return criarRequisicao(path, accessToken);
    }

    private static Request.Builder criarRequisicao(
            String path,
            String accessToken
    ) {
        String url = BuildConfig.SUPABASE_URL;
        String key = BuildConfig.SUPABASE_PUBLISHABLE_KEY;

        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException(
                    "SUPABASE_URL vazia no BuildConfig. Confira local.properties na raiz do projeto aberto no Android Studio e execute Sync Project with Gradle Files."
            );
        }

        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException(
                    "SUPABASE_PUBLISHABLE_KEY vazia no BuildConfig. Confira local.properties na raiz do projeto aberto no Android Studio e execute Sync Project with Gradle Files."
            );
        }

        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException(
                    "O caminho deve começar com /."
            );
        }

        String baseUrl = url.trim().replaceAll("/+$", "");
        String apiKey = key.trim();

        if (!baseUrl.startsWith("https://")) {
            throw new IllegalStateException(
                    "A URL do Supabase deve utilizar HTTPS."
            );
        }

        Request.Builder builder = new Request.Builder()
                .url(baseUrl + path)
                .header("apikey", apiKey)
                .header("Accept", "application/json");

        if (accessToken != null) {
            builder.header(
                    "Authorization",
                    "Bearer " + accessToken.trim()
            );
        }

        return builder;
    }
}