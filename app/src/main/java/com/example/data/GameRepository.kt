package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allStories: Flow<List<SavedStory>> = gameDao.getAllSavedStories()
    val recentHistory: Flow<List<GameHistory>> = gameDao.getRecentGameHistory()

    suspend fun saveStory(story: SavedStory) {
        gameDao.insertStory(story)
    }

    suspend fun deleteStory(id: Long) {
        gameDao.deleteStory(id)
    }

    suspend fun recordGame(history: GameHistory) {
        gameDao.insertGameHistory(history)
    }
}
