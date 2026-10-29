package com.meupersonal.service;

import com.meupersonal.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public final class SupabaseClient {

    private static final OkHttpClient CLIENTE = new OkHttpClient();

    private SupabaseClient() {
    }

    public static OkHttpClient getCliente() {
        return CLIENTE;
    }

    public static Request.Builder novaRequisicao(String caminho) {
        String url = BuildConfig.SUPABASE_URL;
        String chave = BuildConfig.SUPABASE_PUBLISHABLE_KEY;

        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException(
                    "SUPABASE_URL não configurada no local.properties"
            );
        }

        if (chave == null || chave.trim().isEmpty()) {
            throw new IllegalStateException(
                    "SUPABASE_PUBLISHABLE_KEY não configurada no local.properties"
            );
        }

        return new Request.Builder()
                .url(url.replaceAll("/+$", "") + caminho)
                .addHeader("apikey", chave);
    }
}