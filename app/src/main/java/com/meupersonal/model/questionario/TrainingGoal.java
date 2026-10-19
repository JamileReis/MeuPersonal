package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.meupersonal.model.user.ExperienceLevel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "objetivos_treino")
public class TrainingGoal {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private GoalType tipo;
    private String descricao;
    private Integer prioridade;

    public boolean isCompatibleWith(ExperienceLevel nivel) {
        return nivel != null
                && prioridade != null
                && prioridade >= 1
                && prioridade <= 5;
    }
}
