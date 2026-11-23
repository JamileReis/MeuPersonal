package com.meupersonal.service.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.meupersonal.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class GeminiImageService {
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-2.5-flash-image:generateContent";
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final String apiKey;

    public GeminiImageService() {
        this(BuildConfig.GEMINI_API_KEY);
    }

    public GeminiImageService(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                    "GEMINI_TRAINING_API_KEY não configurada. Configure-a em local.properties.");
        }
        this.apiKey = apiKey.trim();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(210, TimeUnit.SECONDS)
                .build();
    }

    public Bitmap generateExerciseImage(String prompt) throws IOException {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("O prompt da imagem é obrigatório.");
        }

        JSONObject payload;
        try {
            JSONArray parts = new JSONArray().put(new JSONObject().put("text", prompt.trim()));
            JSONObject content = new JSONObject().put("parts", parts);
            payload = new JSONObject()
                    .put("contents", new JSONArray().put(content))
                    .put("generationConfig", new JSONObject()
                            .put("responseModalities", new JSONArray().put("TEXT").put("IMAGE")));
        } catch (JSONException exception) {
            throw new IOException("Erro ao preparar a solicitação de imagem Gemini.", exception);
        }

        Request request = new Request.Builder()
                .url(API_URL)
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException(extractApiError(responseBody, response.code()));
            }
            if (responseBody.isEmpty()) {
                throw new IOException("O Gemini retornou uma resposta vazia.");
            }

            try {
                JSONObject result = new JSONObject(responseBody);
                JSONArray candidates = result.optJSONArray("candidates");
                if (candidates != null && candidates.length() > 0) {
                    JSONObject candidate = candidates.optJSONObject(0);
                    JSONObject content = candidate == null ? null : candidate.optJSONObject("content");
                    JSONArray returnedParts = content == null ? null : content.optJSONArray("parts");
                    if (returnedParts != null) {
                        for (int i = 0; i < returnedParts.length(); i++) {
                            JSONObject part = returnedParts.optJSONObject(i);
                            JSONObject inlineData = part == null ? null : part.optJSONObject("inlineData");
                            if (inlineData == null && part != null) {
                                inlineData = part.optJSONObject("inline_data");
                            }
                            if (inlineData != null) {
                                String encoded = inlineData.optString("data", "");
                                if (!encoded.isEmpty()) {
                                    byte[] imageBytes;
                                    try {
                                        imageBytes = Base64.decode(encoded, Base64.DEFAULT);
                                    } catch (IllegalArgumentException exception) {
                                        throw new IOException("Os dados da imagem Gemini são inválidos.", exception);
                                    }
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(
                                            imageBytes, 0, imageBytes.length);
                                    if (bitmap != null) return bitmap;
                                    throw new IOException("Não foi possível converter a imagem Gemini em Bitmap.");
                                }
                            }
                        }
                    }
                }
                throw new IOException("O Gemini respondeu, mas não retornou dados de imagem. Tente novamente.");
            } catch (JSONException exception) {
                throw new IOException("Resposta JSON inválida do Gemini.", exception);
            }
        }
    }

    private String extractApiError(String response, int statusCode) {
        String message = "A API do Gemini não forneceu detalhes.";
        try {
            JSONObject json = new JSONObject(response);
            JSONObject error = json.optJSONObject("error");
            if (error != null) message = error.optString("message", message);
        } catch (JSONException ignored) {
            if (response != null && !response.trim().isEmpty()) message = response;
        }
        if (statusCode == 429) {
            return "Limite de uso da API Gemini atingido (HTTP 429). Verifique a cota do projeto. " + message;
        }
        return "Gemini HTTP " + statusCode + ": " + message;
    }
}

