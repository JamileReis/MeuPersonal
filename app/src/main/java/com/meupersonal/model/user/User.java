package com.meupersonal.model.user;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "usuarios"
)
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "nome")
    private String nome;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "ativo")
    private boolean ativo = true;

    public User(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}