package com.meupersonal.model.user;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;


public abstract class BaseEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "criado_em")
    private long criadoEm = System.currentTimeMillis();

    @ColumnInfo(name = "atualizado_em")
    private long atualizadoEm = System.currentTimeMillis();

    public void registrarAtualizacao() {
        this.atualizadoEm = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(long criadoEm) {
        this.criadoEm = criadoEm;
    }

    public long getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(long atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}