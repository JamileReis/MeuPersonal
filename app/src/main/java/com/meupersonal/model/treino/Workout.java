package com.meupersonal.model.treino;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(
        tableName = "treinos",
        foreignKeys = @ForeignKey(
                entity = WorkoutPlan.class,
                parentColumns = "id",
                childColumns = "workoutPlanId",
                onDelete = ForeignKey.CASCADE
        )
)
public class Workout {

    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Long workoutPlanId;
    private String nome;
    private String descricao;
    private DayOfWeek diaDaSemana;
    private Integer duracaoMinutos;
    @androidx.room.Ignore
    private List<Exercise> exercises = new ArrayList<>();

    public void addExercise(Exercise exercise) {
        if (exercise != null) {
            exercises.add(exercise);
        }
    }

    public void removeExercise(Long id) {
        if (id == null) {
            return;
        }

        for (int i = 0; i < exercises.size(); i++) {
            Exercise exercise = exercises.get(i);

            if (id.equals(exercise.getId())) {
                exercises.remove(i);
                return;
            }
        }
    }

    public Integer calculateDuration() {
        return duracaoMinutos != null ? duracaoMinutos : 0;
    }

    public void start() {
        System.out.println("Treino iniciado: " + nome);
    }

    public void finish() {
        System.out.println("Treino finalizado: " + nome);
    }
}