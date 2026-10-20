package com.meupersonal.model.treino;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "exercicios")
public class Exercise {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String nome;
    private String descricao;
    private MuscleGroup misturaGrupo;
    private String instrucao;
    private Integer series;
    private Integer repeticoes;
    private Integer segundosDescanso;
    private String intensidade;
    private Double carga;

    public Long getId() {
        return id;
    }
    public Double calcularVolume() {
        if (series == null || repeticoes == null) {
            return 0.0;
        }

        return (double) series * repeticoes;
    }

    public void updateLoad(Double carga) {
        if (carga != null && carga >= 0) {
            this.carga = carga;
        }
    }

    public String getInstructions() {
        return instrucao;
    }
}