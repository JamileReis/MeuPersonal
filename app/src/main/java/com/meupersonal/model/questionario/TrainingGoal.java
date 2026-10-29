package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.meupersonal.model.user.ExperienceLevel;


@Entity(tableName = "objetivos_treino")
public class TrainingGoal {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private GoalType tipo;
    private String descricao;
    private Integer prioridade;

    public TrainingGoal(long id, GoalType tipo, String descricao, Integer prioridade) {
        this.id = id;
        this.tipo = tipo;
        this.descricao = descricao;
        this.prioridade = prioridade;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GoalType getTipo() {
        return tipo;
    }

    public void setTipo(GoalType tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Integer prioridade) {
        this.prioridade = prioridade;
    }

    public boolean isCompatibleWith(ExperienceLevel nivel) {
        return nivel != null
                && prioridade != null
                && prioridade >= 1
                && prioridade <= 5;
    }
}