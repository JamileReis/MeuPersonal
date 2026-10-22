package com.meupersonal.model.treino;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

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


    public ExerciseMedia(Long id, Long exercicioId, String imagemUrl, MediaType tipoMidia, Boolean geradoPorIA) {
        this.id = id;
        this.exercicioId = exercicioId;
        this.imagemUrl = imagemUrl;
        this.tipoMidia = tipoMidia;
        this.geradoPorIA = geradoPorIA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExercicioId() {
        return exercicioId;
    }

    public void setExercicioId(Long exercicioId) {
        this.exercicioId = exercicioId;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public MediaType getTipoMidia() {
        return tipoMidia;
    }

    public void setTipoMidia(MediaType tipoMidia) {
        this.tipoMidia = tipoMidia;
    }

    public Boolean getGeradoPorIA() {
        return geradoPorIA;
    }

    public void setGeradoPorIA(Boolean geradoPorIA) {
        this.geradoPorIA = geradoPorIA;
    }

    public void loadImage() {
        if (imagemUrl != null && !imagemUrl.isBlank()) {
            System.out.println("URL da mídia: " + imagemUrl);
        }
    }

    public boolean isAvailable() {
        return imagemUrl != null && !imagemUrl.isBlank();
    }
}