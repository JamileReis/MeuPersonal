package com.meupersonal.model.historico;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "historico_treinos")
public class WorkoutHistory {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    private Long userId;
    private Long treinoId;
    private Date iniciadoEm;
    private Date finalizadoEm;
    private Boolean concluido;

    public WorkoutHistory(Long id, Long userId, Long treinoId, Date iniciadoEm, Date finalizadoEm, Boolean concluido) {
        this.id = id;
        this.userId = userId;
        this.treinoId = treinoId;
        this.iniciadoEm = iniciadoEm;
        this.finalizadoEm = finalizadoEm;
        this.concluido = concluido;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTreinoId() {
        return treinoId;
    }

    public void setTreinoId(Long treinoId) {
        this.treinoId = treinoId;
    }

    public Date getFinalizadoEm() {
        return finalizadoEm;
    }

    public void setFinalizadoEm(Date finalizadoEm) {
        this.finalizadoEm = finalizadoEm;
    }

    public Date getIniciadoEm() {
        return iniciadoEm;
    }

    public void setIniciadoEm(Date iniciadoEm) {
        this.iniciadoEm = iniciadoEm;
    }

    public Boolean getConcluido() {
        return concluido;
    }

    public void setConcluido(Boolean concluido) {
        this.concluido = concluido;
    }

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