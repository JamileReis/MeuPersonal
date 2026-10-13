package com.meupersonal.model.questionario;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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