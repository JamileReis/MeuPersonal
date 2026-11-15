package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "condicoes_musculoesqueleticas")
public class MusculoskeletalCondition {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String nome;

    private BodyRegion regiaoCorporal;

    private Severity gravidade;

    private String restricao;

    public MusculoskeletalCondition(Long id, String nome, BodyRegion regiaoCorporal, Severity gravidade, String restricao) {
        this.id = id;
        this.nome = nome;
        this.regiaoCorporal = regiaoCorporal;
        this.gravidade = gravidade;
        this.restricao = restricao;
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

    public BodyRegion getRegiaoCorporal() {
        return regiaoCorporal;
    }

    public void setRegiaoCorporal(BodyRegion regiaoCorporal) {
        this.regiaoCorporal = regiaoCorporal;
    }

    public Severity getGravidade() {
        return gravidade;
    }

    public void setGravidade(Severity gravidade) {
        this.gravidade = gravidade;
    }

    public void setRestricao(String restricao) {
        this.restricao = restricao;
    }

    public Long getId() {
        return id;
    }
    public String getRestricao() {
        return restricao;
    }
    public boolean requiresAdaptation() {
        return (restricao != null && !restricao.isBlank())
                || gravidade == Severity.ALTA;
    }

}