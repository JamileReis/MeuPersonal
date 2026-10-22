package com.meupersonal.model.questionario;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(
        tableName = "perfis_musculoesqueleticos",
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
public class MusculoskeletalProfile {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "questionarioId")
    private long questionarioId;

    private boolean temDor;
    private String descricaoDor;
    private BodyRegion localDor;
    private String movimentoRestricao;

    @Ignore
    private List<MusculoskeletalCondition> condicoes =
            new ArrayList<>();

    public MusculoskeletalProfile(long id, long questionarioId, boolean temDor, String descricaoDor, BodyRegion localDor, String movimentoRestricao, List<MusculoskeletalCondition> condicoes) {
        this.id = id;
        this.questionarioId = questionarioId;
        this.temDor = temDor;
        this.descricaoDor = descricaoDor;
        this.localDor = localDor;
        this.movimentoRestricao = movimentoRestricao;
        this.condicoes = condicoes;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQuestionarioId() {
        return questionarioId;
    }

    public void setQuestionarioId(long questionarioId) {
        this.questionarioId = questionarioId;
    }

    public boolean isTemDor() {
        return temDor;
    }

    public void setTemDor(boolean temDor) {
        this.temDor = temDor;
    }

    public String getDescricaoDor() {
        return descricaoDor;
    }

    public void setDescricaoDor(String descricaoDor) {
        this.descricaoDor = descricaoDor;
    }

    public BodyRegion getLocalDor() {
        return localDor;
    }

    public void setLocalDor(BodyRegion localDor) {
        this.localDor = localDor;
    }

    public String getMovimentoRestricao() {
        return movimentoRestricao;
    }

    public void setMovimentoRestricao(String movimentoRestricao) {
        this.movimentoRestricao = movimentoRestricao;
    }

    public List<MusculoskeletalCondition> getCondicoes() {
        return condicoes;
    }

    public void setCondicoes(List<MusculoskeletalCondition> condicoes) {
        this.condicoes = condicoes;
    }

    public Long getId() {
        return id;
    }


    public void addCondicao(MusculoskeletalCondition condicao) {
        if (condicao != null && !condicoes.contains(condicao)) {
            condicoes.add(condicao);
        }
    }

    public void removeCondicao(Long id) {
        if (id == null) {
            return;
        }

        for (int i = 0; i < condicoes.size(); i++) {
            MusculoskeletalCondition condicao = condicoes.get(i);

            if (condicao != null
                    && condicao.getId() != null
                    && condicao.getId().equals(id)) {
                condicoes.remove(i);
                return;
            }
        }
    }

    public boolean hasRestriction() {
        return !getRestrictions().isEmpty();
    }

    public List<String> getRestrictions() {
        List<String> restricoes = new ArrayList<>();

        adicionarRestricao(restricoes, movimentoRestricao);

        for (MusculoskeletalCondition condicao : condicoes) {
            if (condicao != null) {
                adicionarRestricao(
                        restricoes,
                        condicao.getRestricao()
                );
            }
        }

        return restricoes;
    }

    private void adicionarRestricao(
            List<String> restricoes,
            String restricao
    ) {
        if (restricao != null
                && !restricao.isBlank()
                && !restricoes.contains(restricao)) {
            restricoes.add(restricao);
        }
    }
}