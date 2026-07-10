package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM saved_stories ORDER BY timestamp DESC")
    fun getAllSavedStories(): Flow<List<SavedStory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: SavedStory)

    @Query("DELETE FROM saved_stories WHERE id = :id")
    suspend fun deleteStory(id: Long)

    @Query("SELECT * FROM game_history ORDER BY timestamp DESC LIMIT 30")
    fun getRecentGameHistory(): Flow<List<GameHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameHistory(history: GameHistory)
}
