package com.meupersonal.model.treino;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

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

    public Exercise(Long id, String nome, String descricao, MuscleGroup misturaGrupo, String instrucao, Integer series, Integer repeticoes, Integer segundosDescanso, String intensidade, Double carga) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.misturaGrupo = misturaGrupo;
        this.instrucao = instrucao;
        this.series = series;
        this.repeticoes = repeticoes;
        this.segundosDescanso = segundosDescanso;
        this.intensidade = intensidade;
        this.carga = carga;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public MuscleGroup getMisturaGrupo() {
        return misturaGrupo;
    }

    public String getInstrucao() {
        return instrucao;
    }

    public Integer getSeries() {
        return series;
    }

    public Integer getRepeticoes() {
        return repeticoes;
    }

    public Integer getSegundosDescanso() {
        return segundosDescanso;
    }

    public String getIntensidade() {
        return intensidade;
    }

    public Double getCarga() {
        return carga;
    }

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