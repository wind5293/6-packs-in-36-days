package com.example.fitflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitflow.data.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises")
    fun getAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE difficulty = :difficulty")
    fun getByDifficulty(difficulty: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE exercise_type = :type")
    fun getByType(type: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE target_muscles LIKE '%' || :muscle || '%'")
    fun getByMuscle(muscle: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Query("SELECT * FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ExerciseEntity?

    @Query("SELECT local_gifs FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getGifsByName(name: String): String?
  
    @Query("SELECT * FROM exercises WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getByNameIgnoreCase(name: String): ExerciseEntity?
}