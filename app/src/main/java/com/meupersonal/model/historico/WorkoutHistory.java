package com.meupersonal.model.historico;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(tableName = "historico_treinos")
public class WorkoutHistory {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    private Long userId;
    private Long treinoId;
    private Date iniciadoEm;
    private Date finalizadoEm;
    private Boolean concluido;

    public void iniciarTreino() {
        this.iniciadoEm = new Date();
        this.concluido = false;
    }

    public void finalizarTreino() {
        this.finalizadoEm = new Date();
        this.concluido = true;
    }

    public Long calcularDuracao() {
        if (iniciadoEm == null || finalizadoEm == null) {
            return 0L;
        }

        return (finalizadoEm.getTime() - iniciadoEm.getTime())
                / (1000 * 60);
    }
}