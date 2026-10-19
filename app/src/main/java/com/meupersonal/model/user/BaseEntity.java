package com.meupersonal.model.user;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
<<<<<<< HEAD
=======

>>>>>>> master
}