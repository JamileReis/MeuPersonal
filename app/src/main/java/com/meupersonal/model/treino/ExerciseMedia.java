package com.meupersonal.model.treino;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(
        tableName = "midias_exercicio",
        foreignKeys = @ForeignKey(
                entity = Exercise.class,
                parentColumns = "id",
                childColumns = "exerciseId",
                onDelete = ForeignKey.CASCADE
        )
)
public class ExerciseMedia {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Long exercicioId;
    private String imagemUrl;
    private MediaType tipoMidia;
    private Boolean geradoPorIA;

    public void loadImage() {
        if (imagemUrl != null && !imagemUrl.isBlank()) {
            System.out.println("URL da mídia: " + imagemUrl);
        }
    }

    public boolean isAvailable() {
        return imagemUrl != null && !imagemUrl.isBlank();
    }
}