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
            "SELECT * FROM treinos " +
                    "WHERE workoutPlanId IN (" +
                    "SELECT id FROM planos_treino WHERE userID = :usuarioId" +
                    ")"
    )
    List<Workout> findByUsuarioId(Long usuarioId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save(Workout treino);

    @Query("DELETE FROM treinos WHERE id = :id")
    void delete(Long id);

    @Query(
            "SELECT * FROM treinos " +
                    "WHERE workoutPlanId IN (" +
                    "SELECT id FROM planos_treino WHERE status = 'ATIVO'" +
                    ")"
    )
    List<Workout> findActive();
}