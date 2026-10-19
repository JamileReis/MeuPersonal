package com.meupersonal.model.questionario;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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

    public void addCondition(MusculoskeletalCondition condicao) {
        if (condicao != null && !condicoes.contains(condicao)) {
            condicoes.add(condicao);
        }
    }

    public void removeCondition(Long id) {
        if (id == null) {
            return;
        }

        condicoes.removeIf(condicao ->
                condicao != null
                        && Objects.equals(condicao.getId(), id)
        );
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