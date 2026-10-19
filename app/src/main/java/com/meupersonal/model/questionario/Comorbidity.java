package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "comorbidades")
public class Comorbidity {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private String nome;

    private String descricao;

    private Severity gravidade;

    public boolean isRelevantToTraining() {
        return gravidade == Severity.MODERATE
                || gravidade == Severity.HIGH;
    }

    public String getDescription() {
        return descricao;
    }
}