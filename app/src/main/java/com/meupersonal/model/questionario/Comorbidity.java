package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comorbidades")
public class Comorbidity {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String nome;

    private String descricao;

    private Severity gravidade;

    public Comorbidity(Long id, String nome, String descricao, Severity gravidade) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.gravidade = gravidade;
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

    public Severity getGravidade() {
        return gravidade;
    }

    public void setGravidade(Severity gravidade) {
        this.gravidade = gravidade;
    }

    public boolean isRelevantToTraining() {
        return gravidade == Severity.MODERADA
                || gravidade == Severity.ALTA;
    }

    public String getDescription() {
        return descricao;
    }
}