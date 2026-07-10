package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_stories")
data class SavedStory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val storyText: String,
    val themeName: String,
    val isMultiplayer: Boolean,
    val roomCode: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameType: String, // "SUPER_TIC_TAC_TOE" or "MADLIBS"
    val mode: String, // "AI", "ROOM"
    val roomCode: String = "",
    val result: String, // "PLAYER_X_WIN", "PLAYER_O_WIN", "AI_WIN", "DRAW", "COMPLETED"
    val movesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
