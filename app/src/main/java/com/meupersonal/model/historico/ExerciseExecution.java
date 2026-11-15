package com.meupersonal.model.historico;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import com.meupersonal.model.treino.Exercise;

@Entity(
        tableName = "execucoes_exercicio",
        foreignKeys = {
                @ForeignKey(
                        entity = WorkoutHistory.class,
                        parentColumns = "id",
                        childColumns = "historiaId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Exercise.class,
                        parentColumns = "id",
                        childColumns = "exercicioId",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class ExerciseExecution {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private Long exercicioId;
    private Long historiaId;
    private Integer seriesRealizadas;
    private Integer repeticoesRealizadas;
    private Double cargaUtilizada;
    private Integer esforcoPercebido;
    private String observacoes;

    public ExerciseExecution(Long id, Long exercicioId, Long historiaId, Integer seriesRealizadas, Integer repeticoesRealizadas, Double cargaUtilizada, Integer esforcoPercebido, String observacoes) {
        this.id = id;
        this.exercicioId = exercicioId;
        this.historiaId = historiaId;
        this.seriesRealizadas = seriesRealizadas;
        this.repeticoesRealizadas = repeticoesRealizadas;
        this.cargaUtilizada = cargaUtilizada;
        this.esforcoPercebido = esforcoPercebido;
        this.observacoes = observacoes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExercicioId() {
        return exercicioId;
    }

    public void setExercicioId(Long exercicioId) {
        this.exercicioId = exercicioId;
    }

    public Long getHistoriaId() {
        return historiaId;
    }

    public void setHistoriaId(Long historiaId) {
        this.historiaId = historiaId;
    }

    public Integer getSeriesRealizadas() {
        return seriesRealizadas;
    }

    public void setSeriesRealizadas(Integer seriesRealizadas) {
        this.seriesRealizadas = seriesRealizadas;
    }

    public Integer getRepeticoesRealizadas() {
        return repeticoesRealizadas;
    }

    public void setRepeticoesRealizadas(Integer repeticoesRealizadas) {
        this.repeticoesRealizadas = repeticoesRealizadas;
    }

    public Double getCargaUtilizada() {
        return cargaUtilizada;
    }

    public void setCargaUtilizada(Double cargaUtilizada) {
        this.cargaUtilizada = cargaUtilizada;
    }

    public Integer getEsforcoPercebido() {
        return esforcoPercebido;
    }

    public void setEsforcoPercebido(Integer esforcoPercebido) {
        this.esforcoPercebido = esforcoPercebido;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public void registrarSerie() {
        this.seriesRealizadas = seriesRealizadas == null
                ? 1
                : seriesRealizadas + 1;
    }

    public Double calcularVolume() {
        if (seriesRealizadas == null
                || repeticoesRealizadas == null
                || cargaUtilizada == null) {
            return 0.0;
        }

        return seriesRealizadas * repeticoesRealizadas * cargaUtilizada;
    }

    public Integer calcularTotalRepeticoes() {
        if (seriesRealizadas == null || repeticoesRealizadas == null) {
            return 0;
        }

        return seriesRealizadas * repeticoesRealizadas;
    }
}