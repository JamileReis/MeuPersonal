package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(
        tableName = "perfis_saude",
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
public class HealthProfile {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    private Long questionarioId;
    private Boolean historicoFamiliar;
    private Boolean habitosAlimentares;
    private Boolean historicoIntervencoes;
    private String restricoes;

    public boolean validar() {
        return questionarioId != null && questionarioId > 0;
    }

    public boolean possuiRestricoes() {
        return restricoes != null && !restricoes.isBlank();
    }
}
