package com.meupersonal.model.treino;

import android.os.Build;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Entity(
        tableName = "treinos",
        foreignKeys = @ForeignKey(
                entity = WorkoutPlan.class,
                parentColumns = "id",
                childColumns = "workoutPlanId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "workoutPlanId")
        }
)
public class Workout {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private Long workoutPlanId;
    private String nome;
    private String descricao;
    private DayOfWeek diaDaSemana;
    private Integer duracaoMinutos;

    @Ignore
    private List<Exercise> exercises = new ArrayList<>();

    public Workout() {
        this.exercises = new ArrayList<>();
    }

    @Ignore
    public Workout(
            Long id,
            List<Exercise> exercises,
            DayOfWeek diaDaSemana,
            Integer duracaoMinutos,
            String descricao,
            String nome,
            Long workoutPlanId
    ) {
        this.id = id;
        this.exercises = exercises != null
                ? new ArrayList<>(exercises)
                : new ArrayList<>();
        this.diaDaSemana = diaDaSemana;
        this.duracaoMinutos = duracaoMinutos;
        this.descricao = descricao;
        this.nome = nome;
        this.workoutPlanId = workoutPlanId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkoutPlanId() {
        return workoutPlanId;
    }

    public void setWorkoutPlanId(Long workoutPlanId) {
        this.workoutPlanId = workoutPlanId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public DayOfWeek getDiaDaSemana() {
        return diaDaSemana;
    }

    public void setDiaDaSemana(DayOfWeek diaDaSemana) {
        this.diaDaSemana = diaDaSemana;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public List<Exercise> getExercises() {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises != null
                ? new ArrayList<>(exercises)
                : new ArrayList<>();
    }

    public void addExercise(Exercise exercise) {
        if (exercise != null) {
            getExercises().add(exercise);
        }
    }

    public void removeExercise(Long id) {
        if (id == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getExercises().removeIf(
                    exercise -> id.equals(exercise.getId())
            );
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