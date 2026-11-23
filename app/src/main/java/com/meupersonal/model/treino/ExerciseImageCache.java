package com.meupersonal.model.treino;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise_image_cache")

public class ExerciseImageCache {
    @PrimaryKey
    @NonNull
    private String cacheKey;
    private long planId;
    private int dayIndex;
    private int exerciseIndex;
    @NonNull
    private byte[] image;

    public ExerciseImageCache(@NonNull String cacheKey, long planId, int dayIndex,
                              int exerciseIndex, @NonNull byte[] image) {
        this.cacheKey = cacheKey;
        this.planId = planId;
        this.dayIndex = dayIndex;
        this.exerciseIndex = exerciseIndex;
        this.image = image;
    }
    @NonNull public String getCacheKey() { return cacheKey; }
    public long getPlanId() { return planId; }
    public int getDayIndex() { return dayIndex; }
    public int getExerciseIndex() { return exerciseIndex; }
    @NonNull public byte[] getImage() { return image; }
}
