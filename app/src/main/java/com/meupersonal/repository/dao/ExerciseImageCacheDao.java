package com.meupersonal.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.meupersonal.model.treino.ExerciseImageCache;

@Dao
public interface ExerciseImageCacheDao {
    @Query("SELECT image FROM exercise_image_cache WHERE cacheKey = :cacheKey LIMIT 1")
    byte[] getImage(String cacheKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(ExerciseImageCache image);
}

