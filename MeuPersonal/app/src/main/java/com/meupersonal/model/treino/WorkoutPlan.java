package com.meupersonal.model.treino;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.meupersonal.model.questionario.GoalType;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Entity(tableName = "planos_treino")

public class WorkoutPlan {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private Long userID;
    private String nome;
    private GoalType objetivo;
    private Date geradoEm;
    private PlanStatus status;

    @Ignore
    private List<Workout> workouts = new ArrayList<>();

    public WorkoutPlan() {
    }

    @Ignore
    public WorkoutPlan(
            Long id,
            Long userID,
            String nome,
            GoalType objetivo,
            Date geradoEm,
            PlanStatus status
    ) {
        this.id = id;
        this.userID = userID;
        this.nome = nome;
        this.objetivo = objetivo;
        this.geradoEm = geradoEm;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public GoalType getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(GoalType objetivo) {
        this.objetivo = objetivo;
    }

    public Date getGeradoEm() {
        return geradoEm;
    }

    public void setGeradoEm(Date geradoEm) {
        this.geradoEm = geradoEm;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public List<Workout> getWorkouts() {
        if (workouts == null) {
            workouts = new ArrayList<>();
        }
        return workouts;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts == null
                ? new ArrayList<>()
                : new ArrayList<>(workouts);
    }

    public void addWorkout(Workout workout) {
        if (workout == null) {
            throw new IllegalArgumentException("O treino não pode ser nulo.");
        }
        getWorkouts().add(workout);
    }

    public void removeWorkout(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do treino não pode ser nulo.");
        }

        Iterator<Workout> iterator = getWorkouts().iterator();

        while (iterator.hasNext()) {
            Workout workout = iterator.next();

            if (id.equals(workout.getId())) {
                iterator.remove();
            }
        }
    }

    public void ativar() {
        this.status = PlanStatus.ATIVO;
    }

    public void desativar() {
        this.status = PlanStatus.INATIVO;
    }

    public Integer getTotalWorkouts() {
        return getWorkouts().size();
    }
}