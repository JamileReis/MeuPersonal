package com.meupersonal.service.media;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public final class MediaGenerationService {
    public interface ImageCallback {
        void onSuccess(Bitmap bitmap);
        void onError(String message);
    }

    private final GeminiImageService imageService;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public MediaGenerationService() {
        this.imageService = new GeminiImageService();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void generateExerciseImage(
            String prompt,
            ImageCallback callback
    ) {
        if (callback == null) {
            throw new IllegalArgumentException(
                    "Callback obrigatório."
            );
        }

        if (prompt == null || prompt.trim().isEmpty()) {
            mainHandler.post(() ->
                    callback.onError(
                            "A descrição do exercício é obrigatória."
                    )
            );
            return;
        }

        executor.execute(() -> {
            try {
                Bitmap bitmap =
                        imageService.generateExerciseImage(prompt.trim());

                mainHandler.post(() -> {
                    if (!executor.isShutdown()) {
                        callback.onSuccess(bitmap);
                    }
                });

            } catch (IOException | IllegalArgumentException exception) {
                String message = exception.getMessage();

                mainHandler.post(() -> {
                    if (!executor.isShutdown()) {
                        callback.onError(
                                message != null
                                        ? message
                                        : "Falha ao gerar imagem pelo Gemini."
                        );
                    }
                });
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}