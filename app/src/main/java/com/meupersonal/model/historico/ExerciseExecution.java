package com.meupersonal.model.historico;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import com.meupersonal.model.treino.Exercise;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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