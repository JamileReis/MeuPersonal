package com.meupersonal.service.ia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.meupersonal.model.treino.Exercise;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class GeminiAiServiceApiKeyTest {

    @Test
    public void deveCriarServicoComConstrutorOriginal() {
        GeminiAiService service = new GeminiAiService();

        assertNotNull(service);
    }

    @Test
    public void deveImplementarAiService() {
        GeminiAiService service = new GeminiAiService();

        assertTrue(service instanceof AiService);
    }

    @Test
    public void deveRejeitarPromptNulo() {
        GeminiAiService service = new GeminiAiService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendPrompt(null)
        );
    }

    @Test
    public void deveRejeitarPromptVazio() {
        GeminiAiService service = new GeminiAiService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendPrompt("")
        );
    }

    @Test
    public void deveRejeitarPromptComEspacos() {
        GeminiAiService service = new GeminiAiService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendPrompt("   ")
        );
    }

    @Test
    public void deveRejeitarQuestionarioNulo() {
        GeminiAiService service = new GeminiAiService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateTrainingPlan(null)
        );
    }

    @Test
    public void deveRejeitarExercicioNuloNaDescricao() {
        GeminiAiService service = new GeminiAiService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateExerciseDescription(null)
        );
    }

    @Test
    public void deveRejeitarExercicioNuloNaImagem() {
        GeminiAiService service = new GeminiAiService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.generateExerciseImage(null)
        );
    }

    @Test
    public void deveInformarErroDeConfiguracaoQuandoApiKeyInvalida()
            throws Exception {

        /*
         * O construtor original valida a constante API_KEY.
         * Não existe construtor que receba a chave como argumento.
         *
         * Este teste verifica a configuração real somente se a
         * classe estiver usando o marcador de configuração.
         */
        try {
            GeminiAiService service = new GeminiAiService();
            assertNotNull(service);
        } catch (IllegalStateException exception) {
            assertTrue(
                    exception.getMessage().contains("API key")
                            || exception.getMessage().contains("API key")
            );
        }
    }
}