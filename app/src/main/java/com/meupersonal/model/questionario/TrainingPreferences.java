package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

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

    public TrainingPreferences(long id, List<Equipment> equipamentos, TrainingLocation localPreferido, List<DayOfWeek> diasPreferidos, Integer duracaoMinutos, Integer frequencia) {
        this.id = id;
        this.equipamentos = equipamentos;
        this.localPreferido = localPreferido;
        this.diasPreferidos = diasPreferidos;
        this.duracaoMinutos = duracaoMinutos;
        this.frequencia = frequencia;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(Integer frequencia) {
        this.frequencia = frequencia;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public List<DayOfWeek> getDiasPreferidos() {
        return diasPreferidos;
    }

    public void setDiasPreferidos(List<DayOfWeek> diasPreferidos) {
        this.diasPreferidos = diasPreferidos;
    }

    public TrainingLocation getLocalPreferido() {
        return localPreferido;
    }

    public void setLocalPreferido(TrainingLocation localPreferido) {
        this.localPreferido = localPreferido;
    }

    public List<Equipment> getEquipamentos() {
        return equipamentos;
    }

    public void setEquipamentos(List<Equipment> equipamentos) {
        this.equipamentos = equipamentos;
    }

    public boolean hasEquipment() {
        return equipamentos != null && !equipamentos.isEmpty();
    }

    public boolean hasEquipment(Equipment equipamentoNecessario) {
        return equipamentos != null
                && equipamentos.contains(equipamentoNecessario);
    }
}