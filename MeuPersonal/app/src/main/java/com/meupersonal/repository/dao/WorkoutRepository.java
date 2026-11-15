package com.meupersonal.repository.dao;

import androidx.room.Dao;

import com.meupersonal.model.treino.Workout;

import java.util.List;
@Dao
public class WorkoutRepository{

    private final WorkoutDao workoutDao;

    public WorkoutRepository(WorkoutDao workoutDao) {
        if (workoutDao == null) {
            throw new IllegalArgumentException(
                    "WorkoutDao não pode ser nulo."
            );
        }

        this.workoutDao = workoutDao;
    }

    public List<Workout> findByUsuarioId(Long usuarioId) {
        validarUsuario(usuarioId);
        return workoutDao.findByUsuarioId(usuarioId);
    }

    public List<Workout> findActive(Long usuarioId) {
        validarUsuario(usuarioId);
        return workoutDao.findActive(usuarioId);
    }

    public long save(Workout workout) {
        if (workout == null) {
            throw new IllegalArgumentException(
                    "O treino não pode ser nulo."
            );
        }

        return workoutDao.save(workout);
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID do treino inválido."
            );
        }

        workoutDao.delete(id);
    }

    private void validarUsuario(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "ID do usuário inválido."
            );
        }
    }
}