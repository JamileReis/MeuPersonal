package com.meupersonal.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.meupersonal.model.treino.Workout;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Query(
            "SELECT treinos.* FROM treinos " +
                    "INNER JOIN planos_treino " +
                    "ON treinos.workoutPlanId = planos_treino.id " +
                    "WHERE planos_treino.userID = :usuarioId"
    )
    List<Workout> findByUsuarioId(Long usuarioId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save(Workout workout);

    @Query("DELETE FROM treinos WHERE id = :id")
    void delete(Long id);

    @Query(
            "SELECT treinos.* FROM treinos " +
                    "INNER JOIN planos_treino " +
                    "ON treinos.workoutPlanId = planos_treino.id " +
                    "WHERE planos_treino.status = 'ATIVO' " +
                    "AND planos_treino.userID = :usuarioId"
    )
    List<Workout> findActive(Long usuarioId);
}