package com.meupersonal.service.treino;

import com.meupersonal.model.questionario.Questionnaire;
import com.meupersonal.model.questionario.TrainingGoal;
import com.meupersonal.model.treino.Exercise;
import com.meupersonal.model.treino.Workout;
import com.meupersonal.model.treino.WorkoutPlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrainingPlanValidator {
    public ValidationResult validatePlan(WorkoutPlan plan) {
        ValidationResult result = new ValidationResult();

        if (plan == null) {
            result.addMessage("O plano de treino não foi informado.");
            return result;
        }

        if (plan.getUserID() == null || plan.getUserID() <= 0) {
            result.addMessage("O plano deve estar associado a um usuário válido.");
        }

        if (isBlank(plan.getNome())) {
            result.addMessage("O plano precisa ter um nome.");
        }

        if (plan.getObjetivo() == null) {
            result.addMessage("O plano precisa ter um objetivo.");
        }

        if (plan.getGeradoEm() == null) {
            result.addMessage("A data de geração é obrigatória.");
        }

        if (plan.getStatus() == null) {
            result.addMessage("O status do plano é obrigatório.");
        }

        return result;
    }

    public ValidationResult validatePlan(
            WorkoutPlan plan,
            List<Workout> workouts) {

        ValidationResult result = validatePlan(plan);

        if (workouts == null || workouts.isEmpty()) {
            result.addMessage("O plano precisa conter pelo menos um treino.");
            return result;
        }

        for (int i = 0; i < workouts.size(); i++) {
            Workout workout = workouts.get(i);

            if (workout == null) {
                result.addMessage("O treino na posição " + i + " é inválido.");
                continue;
            }

            ValidationResult workoutResult = validateWorkout(workout);

            for (String message : workoutResult.getMessages()) {
                result.addMessage(message);
            }

            if (plan != null
                    && plan.getId() != null
                    && workout.getWorkoutPlanId() != null
                    && !plan.getId().equals(workout.getWorkoutPlanId())) {
                result.addMessage(
                        "O treino " + workout.getNome()
                                + " pertence a outro plano."
                );
            }
        }

        return result;
    }

    public ValidationResult validateWorkout(Workout workout) {
        ValidationResult result = new ValidationResult();

        if (workout == null) {
            result.addMessage("O treino não foi informado.");
            return result;
        }

        if (isBlank(workout.getNome())) {
            result.addMessage("O nome do treino é obrigatório.");
        }

        if (workout.getDiaDaSemana() == null) {
            result.addMessage("O dia da semana é obrigatório.");
        }

        if (workout.getDuracaoMinutos() == null
                || workout.getDuracaoMinutos() <= 0) {
            result.addMessage("A duração deve ser maior que zero.");
        }

        List<Exercise> exercises = workout.getExercises();

        if (exercises == null || exercises.isEmpty()) {
            result.addMessage("O treino precisa conter exercícios.");
            return result;
        }

        for (int i = 0; i < exercises.size(); i++) {
            Exercise exercise = exercises.get(i);

            if (exercise == null) {
                result.addMessage(
                        "O exercício na posição " + i + " é inválido."
                );
                continue;
            }

            ValidationResult exerciseResult = validateExercise(exercise);

            for (String message : exerciseResult.getMessages()) {
                result.addMessage(message);
            }
        }

        return result;
    }

    public ValidationResult validateExercise(Exercise exercise) {
        ValidationResult result = new ValidationResult();

        if (exercise == null) {
            result.addMessage("O exercício não foi informado.");
            return result;
        }

        if (isBlank(exercise.getNome())) {
            result.addMessage("O nome do exercício é obrigatório.");
        }

        if (exercise.getSeries() == null || exercise.getSeries() <= 0) {
            result.addMessage("O número de séries deve ser maior que zero.");
        }

        if (exercise.getRepeticoes() == null
                || exercise.getRepeticoes() <= 0) {
            result.addMessage("O número de repetições deve ser maior que zero.");
        }

        if (exercise.getSegundosDescanso() != null
                && exercise.getSegundosDescanso() < 0) {
            result.addMessage("O descanso não pode ser negativo.");
        }

        if (exercise.getCarga() != null && exercise.getCarga() < 0) {
            result.addMessage("A carga não pode ser negativa.");
        }

        return result;
    }

    public ValidationResult validateGoal(TrainingGoal goal) {
        ValidationResult result = new ValidationResult();

        if (goal == null) {
            result.addMessage("O objetivo não foi informado.");
            return result;
        }

        if (goal.getTipo() == null) {
            result.addMessage("O tipo do objetivo é obrigatório.");
        }

        if (isBlank(goal.getDescricao())) {
            result.addMessage("A descrição do objetivo é obrigatória.");
        }

        if (goal.getPrioridade() == null
                || goal.getPrioridade() < 1
                || goal.getPrioridade() > 5) {
            result.addMessage("A prioridade deve estar entre 1 e 5.");
        }

        return result;
    }

    public ValidationResult validateQuestionnaire(
            Questionnaire questionnaire) {

        ValidationResult result = new ValidationResult();

        if (questionnaire == null) {
            result.addMessage("O questionário não foi informado.");
            return result;
        }

        if (!questionnaire.validar()) {
            result.addMessage(
                    "O questionário deve estar associado a um usuário válido."
            );
        }

        if (!questionnaire.isCompleto()) {
            result.addMessage("O questionário precisa estar completo.");
        }

        return result;
    }

    public boolean validateRestrictions(
            WorkoutPlan plan,
            Questionnaire questionnaire) {

        return plan != null
                && questionnaire != null
                && plan.getUserID() != null
                && questionnaire.getUserId() != null
                && plan.getUserID().equals(questionnaire.getUserId());
    }

    public boolean validateVolume(List<Workout> workouts) {
        if (workouts == null || workouts.isEmpty()) {
            return false;
        }

        for (Workout workout : workouts) {
            if (workout == null
                    || workout.getExercises() == null
                    || workout.getExercises().isEmpty()) {
                return false;
            }

            for (Exercise exercise : workout.getExercises()) {
                if (exercise == null
                        || exercise.getSeries() == null
                        || exercise.getSeries() <= 0
                        || exercise.getRepeticoes() == null
                        || exercise.getRepeticoes() <= 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}