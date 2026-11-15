package com.meupersonal.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.meupersonal.model.treino.Exercise;
import com.meupersonal.model.treino.MuscleGroup;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Query(
            "SELECT * FROM exercicios " +
                    "WHERE misturaGrupo = :grupoMuscular"
    )
    List<Exercise> findByMuscleGroup(MuscleGroup grupoMuscular);

    @Query(
            "SELECT * FROM exercicios " +
                    "WHERE nome LIKE '%' || :nome || '%'"
    )
    List<Exercise> findByName(String nome);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save(Exercise exercicio);

    @Query("DELETE FROM exercicios WHERE id = :id")
    void delete(Long id);
}
