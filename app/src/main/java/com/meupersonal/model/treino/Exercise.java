package com.meupersonal.model.treino;

import androidx.room.Entity;
import androidx.room.Ignore;
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

    public Exercise() {
    }

    @Ignore
    public Exercise(
            Long id,
            String nome,
            String descricao,
            MuscleGroup misturaGrupo,
            String instrucao,
            Integer series,
            Integer repeticoes,
            Integer segundosDescanso,
            String intensidade,
            Double carga
    ) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public MuscleGroup getMisturaGrupo() {
        return misturaGrupo;
    }

    public void setMisturaGrupo(MuscleGroup misturaGrupo) {
        this.misturaGrupo = misturaGrupo;
    }

    public String getInstrucao() {
        return instrucao;
    }

    public void setInstrucao(String instrucao) {
        this.instrucao = instrucao;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public Integer getRepeticoes() {
        return repeticoes;
    }

    public void setRepeticoes(Integer repeticoes) {
        this.repeticoes = repeticoes;
    }

    public Integer getSegundosDescanso() {
        return segundosDescanso;
    }

    public void setSegundosDescanso(Integer segundosDescanso) {
        this.segundosDescanso = segundosDescanso;
    }

    public String getIntensidade() {
        return intensidade;
    }

    public void setIntensidade(String intensidade) {
        this.intensidade = intensidade;
    }

    public Double getCarga() {
        return carga;
    }

    public void setCarga(Double carga) {
        this.carga = carga;
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