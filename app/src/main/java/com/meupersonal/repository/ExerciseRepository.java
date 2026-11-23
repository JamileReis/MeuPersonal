package com.meupersonal.repository;

import com.meupersonal.model.treino.Exercise;
import com.meupersonal.model.treino.MuscleGroup;
import com.meupersonal.repository.dao.ExerciseDao;

import java.util.List;
import java.util.Objects;

public class ExerciseRepository {
    private final ExerciseDao exerciseDao;

    public ExerciseRepository(ExerciseDao exerciseDao) {
        this.exerciseDao = Objects.requireNonNull(
                exerciseDao,
                "ExerciseDao não pode ser nulo."
        );
    }

    public List<Exercise> findByMuscleGroup(
            MuscleGroup grupoMuscular
    ) {
        if (grupoMuscular == null) {
            throw new IllegalArgumentException(
                    "O grupo muscular não pode ser nulo."
            );
        }

        return exerciseDao.findByMuscleGroup(grupoMuscular);
    }

    public List<Exercise> findByName(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "O nome do exercício não pode ser vazio."
            );
        }

        return exerciseDao.findByName(nome.trim());
    }

    public void save(Exercise exercicio) {
        Objects.requireNonNull(
                exercicio,
                "O exercício não pode ser nulo."
        );

        exerciseDao.save(exercicio);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID do exercício não pode ser nulo."
            );
        }

        exerciseDao.delete(id);
    }
}