package com.meupersonal.repository;

import com.meupersonal.model.treino.Workout;
import com.meupersonal.repository.dao.WorkoutDao;

import java.util.List;
import java.util.Objects;

public class WorkoutRepository {

    private final WorkoutDao workoutDao;

    public WorkoutRepository(WorkoutDao workoutDao) {
        this.workoutDao = Objects.requireNonNull(
                workoutDao,
                "WorkoutDao não pode ser nulo."
        );
    }

    public List<Workout> findByUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "O ID do usuário não pode ser nulo."
            );
        }

        return workoutDao.findByUsuarioId(usuarioId);
    }

    public long save(Workout workout) {
        Objects.requireNonNull(
                workout,
                "O treino não pode ser nulo."
        );

        return workoutDao.save(workout);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID do treino não pode ser nulo."
            );
        }

        workoutDao.delete(id);
    }

    public List<Workout> findActive(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "ID do usuário inválido."
            );
        }

        return workoutDao.findActive(usuarioId);
    }
}