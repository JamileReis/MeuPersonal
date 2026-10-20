package com.meupersonal.model.treino;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.meupersonal.model.questionario.GoalType;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "planos_treino")
public class WorkoutPlan {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private Long userID;
    private String nome;
    private GoalType objetivo;
    private Date geradoEm;
    private PlanStatus status;

    public void addWorkout(Workout workout) {
    }

    public void removeWorkout(Long id) {
    }

    public void ativar() {
        this.status = PlanStatus.ATIVO;
    }

    public void desativar() {
        this.status = PlanStatus.INATIVO;
    }

    public Integer getTotalWorkouts() {
        return 0;
    }
}