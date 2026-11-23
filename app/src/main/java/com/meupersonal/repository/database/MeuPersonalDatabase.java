package com.meupersonal.repository.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.meupersonal.model.treino.Exercise;
import com.meupersonal.model.treino.Workout;
import com.meupersonal.model.treino.WorkoutPlan;
import com.meupersonal.repository.converter.RoomConverters;
import com.meupersonal.repository.dao.ExerciseDao;
import com.meupersonal.repository.dao.WorkoutDao;

@Database(
        entities = {
                Workout.class,
                Exercise.class,
                WorkoutPlan.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters(RoomConverters.class)
public abstract class MeuPersonalDatabase extends RoomDatabase {
    private static volatile MeuPersonalDatabase instancia;

    public abstract WorkoutDao workoutDao();

    public abstract ExerciseDao exerciseDao();

    public static MeuPersonalDatabase getInstancia(Context contexto) {
        if (instancia == null) {
            synchronized (MeuPersonalDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(
                            contexto.getApplicationContext(),
                            MeuPersonalDatabase.class,
                            "meu_personal_database"
                    ).build();
                }
            }
        }

        return instancia;
    }
}