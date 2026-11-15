package com.meupersonal.service;

import com.meupersonal.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public final class SupabaseClient {

    private static final OkHttpClient CLIENTE = new OkHttpClient();

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private SupabaseClient() { }

    public static OkHttpClient getCliente() { return CLIENT; }

    public static Request.Builder novaRequisicao(String path) {
        String url = BuildConfig.SUPABASE_URL;
        String key = BuildConfig.SUPABASE_PUBLISHABLE_KEY;
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("SUPABASE_URL não configurada no local.properties.");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException("SUPABASE_PUBLISHABLE_KEY não configurada no local.properties.");
        }
        return new Request.Builder()
                .url(url.replaceAll("/+$", "") + path)
                .header("apikey", key)
                .header("Accept", "application/json");
    }
}
