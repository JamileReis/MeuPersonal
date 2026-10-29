package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "perfis_saude",
        foreignKeys = @ForeignKey(
                entity = Questionnaire.class,
                parentColumns = "id",
                childColumns = "questionarioId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = {"questionarioId"}, unique = true)
        }
)
public class HealthProfile {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private Long questionarioId;
    private Boolean historicoFamiliar;
    private Boolean habitosAlimentares;
    private Boolean historicoIntervencoes;
    private String restricoes;

    public HealthProfile(Long id, Long questionarioId, Boolean historicoFamiliar, Boolean habitosAlimentares, Boolean historicoIntervencoes, String restricoes) {
        this.id = id;
        this.questionarioId = questionarioId;
        this.historicoFamiliar = historicoFamiliar;
        this.habitosAlimentares = habitosAlimentares;
        this.historicoIntervencoes = historicoIntervencoes;
        this.restricoes = restricoes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionarioId() {
        return questionarioId;
    }

    public void setQuestionarioId(Long questionarioId) {
        this.questionarioId = questionarioId;
    }

    public Boolean getHistoricoFamiliar() {
        return historicoFamiliar;
    }

    public void setHistoricoFamiliar(Boolean historicoFamiliar) {
        this.historicoFamiliar = historicoFamiliar;
    }

    public Boolean getHabitosAlimentares() {
        return habitosAlimentares;
    }

    public void setHabitosAlimentares(Boolean habitosAlimentares) {
        this.habitosAlimentares = habitosAlimentares;
    }

    public Boolean getHistoricoIntervencoes() {
        return historicoIntervencoes;
    }

    public void setHistoricoIntervencoes(Boolean historicoIntervencoes) {
        this.historicoIntervencoes = historicoIntervencoes;
    }

    public String getRestricoes() {
        return restricoes;
    }

    public void setRestricoes(String restricoes) {
        this.restricoes = restricoes;
    }

    public boolean validar() {
        return questionarioId != null && questionarioId > 0;
    }

    public boolean possuiRestricoes() {
        return restricoes != null && !restricoes.isBlank();
    }
}