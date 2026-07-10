package com.example.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class TicTacToeRoomState(
    val roomCode: String = "",
    val player1Name: String = "Player 1 (Host)",
    val player2Name: String = "Waiting for Player 2...",
    val isPlayer2Connected: Boolean = false,
    val currentTurn: String = "X", // "X" or "O"
    val cells: List<String> = List(81) { "" }, // 81 cell strings: "", "X", "O"
    val miniBoardWinners: List<String> = List(9) { "" }, // 9 mini boards: "", "X", "O", "DRAW"
    val nextRequiredBoard: Int? = null, // null means play anywhere
    val winner: String = "", // "", "X", "O", "DRAW"
    val winningLine: List<Int>? = null,
    val moveCount: Int = 0,
    val lastMoveLog: String = "Room created. Awaiting Player 2."
)

data class MadLibsRoomState(
    val roomCode: String = "",
    val storyTitle: String = "The Cyber Mind Adventure",
    val player1Name: String = "Player 1",
    val player2Name: String = "Player 2",
    val isPlayer2Connected: Boolean = false,
    val prompts: List<MadLibsPromptItem> = emptyList(),
    val filledWords: Map<Int, Pair<String, String>> = emptyMap(), // promptIndex -> Pair(Word, PlayerName)
    val currentPromptIndex: Int = 0,
    val currentTurnPlayer: Int = 1, // 1 for Player 1, 2 for Player 2
    val isCompleted: Boolean = false,
    val finalStoryText: String = ""
)

data class MadLibsPromptItem(
    val index: Int,
    val promptType: String, // e.g., "Noun", "Verb", "Sci-Fi Weapon", "Silly Sound"
    val description: String
)

object RoomSyncManager {
    private val activeTicTacToeRooms = mutableMapOf<String, MutableStateFlow<TicTacToeRoomState>>()
    private val activeMadLibsRooms = mutableMapOf<String, MutableStateFlow<MadLibsRoomState>>()

    val localPlayerId: String = UUID.randomUUID().toString().take(6).uppercase()

    fun getOrCreateTicTacToeRoom(code: String, asHost: Boolean, playerRoleName: String = "Player"): StateFlow<TicTacToeRoomState> {
        val cleanCode = code.trim().uppercase()
        val flow = activeTicTacToeRooms.getOrPut(cleanCode) {
            MutableStateFlow(
                TicTacToeRoomState(
                    roomCode = cleanCode,
                    player1Name = if (asHost) playerRoleName else "Host Mind",
                    player2Name = if (!asHost) playerRoleName else "Waiting for Player 2...",
                    isPlayer2Connected = !asHost
                )
            )
        }

        if (!asHost) {
            val current = flow.value
            flow.value = current.copy(
                player2Name = playerRoleName.ifBlank { "Player 2" },
                isPlayer2Connected = true,
                lastMoveLog = "$playerRoleName joined room $cleanCode! Match initialized."
            )
        }
        return flow.asStateFlow()
    }

    fun updateTicTacToeMove(
        code: String,
        miniBoardIndex: Int,
        cellIndexInMini: Int,
        playerSymbol: String,
        newCells: List<String>,
        newMiniWinners: List<String>,
        nextBoard: Int?,
        overallWinner: String,
        logMessage: String
    ) {
        val cleanCode = code.trim().uppercase()
        val flow = activeTicTacToeRooms[cleanCode] ?: return
        val current = flow.value
        val nextTurn = if (playerSymbol == "X") "O" else "X"
        flow.value = current.copy(
            cells = newCells,
            miniBoardWinners = newMiniWinners,
            nextRequiredBoard = nextBoard,
            currentTurn = nextTurn,
            winner = overallWinner,
            moveCount = current.moveCount + 1,
            lastMoveLog = logMessage
        )
    }

    fun resetTicTacToeRoom(code: String) {
        val cleanCode = code.trim().uppercase()
        val flow = activeTicTacToeRooms[cleanCode] ?: return
        val current = flow.value
        flow.value = current.copy(
            cells = List(81) { "" },
            miniBoardWinners = List(9) { "" },
            nextRequiredBoard = null,
            currentTurn = "X",
            winner = "",
            moveCount = 0,
            lastMoveLog = "Game reset! X starts first."
        )
    }

    fun getOrCreateMadLibsRoom(
        code: String,
        asHost: Boolean,
        storyTitle: String,
        prompts: List<MadLibsPromptItem>,
        playerName: String = "Player"
    ): StateFlow<MadLibsRoomState> {
        val cleanCode = code.trim().uppercase()
        val flow = activeMadLibsRooms.getOrPut(cleanCode) {
            MutableStateFlow(
                MadLibsRoomState(
                    roomCode = cleanCode,
                    storyTitle = storyTitle,
                    player1Name = if (asHost) playerName else "Host Mind",
                    player2Name = if (!asHost) playerName else "Waiting for Player 2...",
                    isPlayer2Connected = !asHost,
                    prompts = prompts
                )
            )
        }

        if (!asHost) {
            val current = flow.value
            flow.value = current.copy(
                player2Name = playerName.ifBlank { "Player 2" },
                isPlayer2Connected = true
            )
        }
        return flow.asStateFlow()
    }

    fun submitMadLibsWord(
        code: String,
        promptIndex: Int,
        word: String,
        playerName: String,
        storyTemplate: (Map<Int, Pair<String, String>>) -> String
    ) {
        val cleanCode = code.trim().uppercase()
        val flow = activeMadLibsRooms[cleanCode] ?: return
        val current = flow.value
        val newMap = current.filledWords.toMutableMap()
        newMap[promptIndex] = Pair(word, playerName)

        val nextIndex = current.currentPromptIndex + 1
        val isFinished = nextIndex >= current.prompts.size
        val nextTurnPlayer = if (current.currentTurnPlayer == 1) 2 else 1

        val finalStoryText = if (isFinished) storyTemplate(newMap) else ""

        flow.value = current.copy(
            filledWords = newMap,
            currentPromptIndex = nextIndex,
            currentTurnPlayer = nextTurnPlayer,
            isCompleted = isFinished,
            finalStoryText = finalStoryText
        )
    }

    fun resetMadLibsRoom(code: String, prompts: List<MadLibsPromptItem>) {
        val cleanCode = code.trim().uppercase()
        val flow = activeMadLibsRooms[cleanCode] ?: return
        val current = flow.value
        flow.value = current.copy(
            prompts = prompts,
            filledWords = emptyMap(),
            currentPromptIndex = 0,
            currentTurnPlayer = 1,
            isCompleted = false,
            finalStoryText = ""
        )
    }
}
