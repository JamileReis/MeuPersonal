package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(
        tableName = "questionarios",
        indices = {@Index(value = {"userId"})}
)
public class Questionnaire {

    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Long userId;
    private Boolean completado = false;
    private Boolean completadoAt;

    public Questionnaire(Long id, Long userId, Boolean completado, Boolean completadoAt) {
        this.id = id;
        this.userId = userId;
        this.completado = completado;
        this.completadoAt = completadoAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Boolean getCompletado() {
        return completado;
    }

    public Boolean getCompletadoAt() {
        return completadoAt;
    }



    public boolean validar() {
        return userId != null && userId > 0;
    }

    public boolean isCompleto() {
        return Boolean.TRUE.equals(completado);
    }

    public void marcarComoCompleto() {
        if (!validar()) {
            throw new IllegalStateException(
                    "O questionário precisa estar associado a um usuário."
            );
        }

        completado = true;
    }
}