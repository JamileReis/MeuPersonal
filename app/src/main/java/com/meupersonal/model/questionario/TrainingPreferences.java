package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.meupersonal.model.treino.Exercise;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "preferencias_treino")
public class TrainingPreferences {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private Integer frequencia;
    private Integer duracaoMinutos;

    @Ignore
    private List<DayOfWeek> diasPreferidos = new ArrayList<>();

    private TrainingLocation localPreferido;

    @Ignore
    private List<Equipment> equipamentos = new ArrayList<>();

    public boolean hasEquipment() {
        return equipamentos != null && !equipamentos.isEmpty();
    }

    public boolean isCompatibleWith(Exercise exercicio) {
        if (exercicio == null) {
            return false;
        }

        Equipment equipamentoNecessario = exercicio.getEquipamento();

        if (equipamentoNecessario == null) {
            return true;
        }

        return equipamentos != null
                && equipamentos.contains(equipamentoNecessario);
    }
}