package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "condicoes_musculoesqueleticas")
public class MusculoskeletalCondition {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String nome;

    private BodyRegion regiaoCorporal;

    private Severity gravidade;

    private String restricao;

    public boolean requiresAdaptation() {
        return (restricao != null && !restricao.isBlank())
                || gravidade == Severity.ALTA;
    }
}