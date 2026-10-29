package com.meupersonal.service.ia;

import com.meupersonal.model.questionario.Questionnaire;
import com.meupersonal.model.treino.Exercise;

import java.io.IOException;

public interface AiService {
    String generateTrainingPlan(Questionnaire questionnaire)
            throws IOException;

    String generateExerciseDescription(Exercise exercise)
            throws IOException;

    String generateExerciseImage(Exercise exercise)
            throws IOException;

    String sendPrompt(String prompt) throws IOException;
}

