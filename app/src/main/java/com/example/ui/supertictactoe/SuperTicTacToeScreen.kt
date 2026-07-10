package com.example.ui.supertictactoe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameHistory
import com.example.data.GameRepository
import com.example.engine3d.TicTacToeGameMode
import com.example.network.GeminiClient
import com.example.network.RoomSyncManager
import com.example.network.TicTacToeRoomState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SuperTicTacToeScreen(
    mode: TicTacToeGameMode,
    roomCode: String,
    aiLevel: String,
    repository: GameRepository,
    onBackToCavern: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val roomCodeClean = roomCode.ifBlank { "MIND-9999" }

    // Connect to room state flow
    val roomStateFlow = remember(roomCodeClean) {
        RoomSyncManager.getOrCreateTicTacToeRoom(
            code = roomCodeClean,
            asHost = true,
            playerRoleName = if (mode == TicTacToeGameMode.AI_BOT) "Human Mind" else "Player 1 (Host)"
        )
    }

    val roomState by roomStateFlow.collectAsState()

    var activePlayerSymbol by remember { mutableStateOf("X") } // Local player's symbol in room testing
    var aiCommentary by remember { mutableStateOf("Cyber-Mind AI is analyzing your cortical decisions...") }
    var isAiThinking by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "sttt_glow")
    val activeGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sttt_glow_pulse"
    )

    // AI Move trigger when mode is AI_BOT and current turn is "O"
    LaunchedEffect(roomState.currentTurn, roomState.winner, mode) {
        if (mode == TicTacToeGameMode.AI_BOT && roomState.currentTurn == "O" && roomState.winner.isEmpty()) {
            isAiThinking = true
            delay((600..1200).random().toLong()) // Realistic thinking delay

            val aiMove = SuperTicTacToeLogic.calculateAiMove(
                cells = roomState.cells,
                miniBoardWinners = roomState.miniBoardWinners,
                nextRequiredBoard = roomState.nextRequiredBoard,
                aiLevel = aiLevel
            )

            if (aiMove != null) {
                val (miniBoardIdx, cellIdxInMini) = aiMove
                val globalCellIdx = miniBoardIdx * 9 + cellIdxInMini

                val newCells = roomState.cells.toMutableList()
                newCells[globalCellIdx] = "O"

                // Check mini board winner
                val startIndex = miniBoardIdx * 9
                val miniCells = newCells.subList(startIndex, startIndex + 9)
                val (miniWinner, _) = SuperTicTacToeLogic.check3x3Winner(miniCells)

                val newMiniWinners = roomState.miniBoardWinners.toMutableList()
                if (miniWinner.isNotEmpty()) {
                    newMiniWinners[miniBoardIdx] = miniWinner
                }

                // Check next required board
                val targetNextBoard = cellIdxInMini
                val nextBoard = if (newMiniWinners[targetNextBoard].isEmpty()) targetNextBoard else null

                // Check overall winner
                val (overallWinner, winLine) = SuperTicTacToeLogic.check3x3Winner(newMiniWinners)

                RoomSyncManager.updateTicTacToeMove(
                    code = roomCodeClean,
                    miniBoardIndex = miniBoardIdx,
                    cellIndexInMini = cellIdxInMini,
                    playerSymbol = "O",
                    newCells = newCells,
                    newMiniWinners = newMiniWinners,
                    nextBoard = nextBoard,
                    overallWinner = overallWinner,
                    logMessage = "Cyber-Mind AI played board $miniBoardIdx cell $cellIdxInMini"
                )

                // Record game in Room DB if finished
                if (overallWinner.isNotEmpty()) {
                    repository.recordGame(
                        GameHistory(
                            gameType = "SUPER_TIC_TAC_TOE",
                            mode = "AI_$aiLevel",
                            roomCode = roomCodeClean,
                            result = if (overallWinner == "X") "PLAYER_X_WIN" else if (overallWinner == "O") "AI_WIN" else "DRAW",
                            movesCount = roomState.moveCount + 1
                        )
                    )
                }

                // AI Live Comment via Gemini API
                if (aiLevel.contains("Gemini")) {
                    coroutineScope.launch {
                        val commentPrompt = "Give a 1-sentence funny sci-fi commentary about playing Super Tic Tac Toe in the mind. Move count: ${roomState.moveCount}."
                        val geminiRes = GeminiClient.generateAiResponse(commentPrompt)
                        if (geminiRes.isNotBlank()) aiCommentary = geminiRes
                    }
                }
            }
            isAiThinking = false
        }
    }

    // Function to handle player tap on a cell
    val onCellTapped: (Int, Int) -> Unit = { miniBoardIdx, cellIdxInMini ->
        val globalCellIdx = miniBoardIdx * 9 + cellIdxInMini

        // Check validity
        val isCorrectBoard = roomState.nextRequiredBoard == null || roomState.nextRequiredBoard == miniBoardIdx
        val isBoardAvailable = roomState.miniBoardWinners[miniBoardIdx].isEmpty()
        val isCellEmpty = roomState.cells[globalCellIdx].isEmpty()
        val isGameOngoing = roomState.winner.isEmpty()

        val canPlayThisTurn = if (mode == TicTacToeGameMode.AI_BOT) {
            roomState.currentTurn == "X"
        } else {
            roomState.currentTurn == activePlayerSymbol
        }

        if (isCorrectBoard && isBoardAvailable && isCellEmpty && isGameOngoing && canPlayThisTurn) {
            val playerSymbol = roomState.currentTurn
            val newCells = roomState.cells.toMutableList()
            newCells[globalCellIdx] = playerSymbol

            // Check mini board winner
            val startIndex = miniBoardIdx * 9
            val miniCells = newCells.subList(startIndex, startIndex + 9)
            val (miniWinner, _) = SuperTicTacToeLogic.check3x3Winner(miniCells)

            val newMiniWinners = roomState.miniBoardWinners.toMutableList()
            if (miniWinner.isNotEmpty()) {
                newMiniWinners[miniBoardIdx] = miniWinner
            }

            // Next required board is cellIdxInMini, unless that board is already full/won
            val targetNextBoard = cellIdxInMini
            val nextBoard = if (newMiniWinners[targetNextBoard].isEmpty()) targetNextBoard else null

            // Check overall winner
            val (overallWinner, _) = SuperTicTacToeLogic.check3x3Winner(newMiniWinners)

            RoomSyncManager.updateTicTacToeMove(
                code = roomCodeClean,
                miniBoardIndex = miniBoardIdx,
                cellIndexInMini = cellIdxInMini,
                playerSymbol = playerSymbol,
                newCells = newCells,
                newMiniWinners = newMiniWinners,
                nextBoard = nextBoard,
                overallWinner = overallWinner,
                logMessage = "$playerSymbol played board $miniBoardIdx cell $cellIdxInMini"
            )

            if (overallWinner.isNotEmpty()) {
                coroutineScope.launch {
                    repository.recordGame(
                        GameHistory(
                            gameType = "SUPER_TIC_TAC_TOE",
                            mode = if (mode == TicTacToeGameMode.AI_BOT) "AI_$aiLevel" else "ROOM",
                            roomCode = roomCodeClean,
                            result = if (overallWinner == "X") "PLAYER_X_WIN" else if (overallWinner == "O") "PLAYER_O_WIN" else "DRAW",
                            movesCount = roomState.moveCount + 1
                        )
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackToCavern,
                    modifier = Modifier.testTag("tictactoe_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF00F0FF)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SUPER TIC-TAC-TOE 3D",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (mode == TicTacToeGameMode.AI_BOT) "VS AI BOT [$aiLevel]" else "ONLINE ROOM CODE: $roomCodeClean",
                        color = Color(0xFF00F0FF),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { RoomSyncManager.resetTicTacToeRoom(roomCodeClean) },
                    modifier = Modifier.testTag("tictactoe_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Match",
                        tint = Color(0xFFFF007A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Turn & Status Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (roomState.currentTurn == "X") Color(0xFF00F0FF) else Color(0xFFFF007A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player X Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            color = Color(0xFF00F0FF).copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00F0FF))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("X", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("PLAYER X", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(if (mode == TicTacToeGameMode.AI_BOT) "Human Mind" else roomState.player1Name, color = Color.Gray, fontSize = 10.sp)
                        }
                    }

                    // Active Status Pulse Indicator
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (roomState.winner.isNotEmpty()) "WINNER: ${roomState.winner}" else "TURN: PLAYER ${roomState.currentTurn}",
                            color = if (roomState.currentTurn == "X") Color(0xFF00F0FF) else Color(0xFFFF007A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (isAiThinking) {
                            Text("AI Thinking...", color = Color(0xFF00FFCC), fontSize = 10.sp)
                        }
                    }

                    // Player O Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PLAYER O", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(if (mode == TicTacToeGameMode.AI_BOT) "AI Bot" else roomState.player2Name, color = Color.Gray, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            modifier = Modifier.size(36.dp),
                            color = Color(0xFFFF007A).copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF007A))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("O", color = Color(0xFFFF007A), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            if (mode == TicTacToeGameMode.ONLINE_ROOM) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Testing online turn? Active role: $activePlayerSymbol",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    OutlinedButton(
                        onClick = { activePlayerSymbol = if (activePlayerSymbol == "X") "O" else "X" },
                        modifier = Modifier.testTag("switch_turn_role_button")
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Switch")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch Role to $activePlayerSymbol", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main 3x3x3 Super Tic-Tac-Toe Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFF080D1A), shape = RoundedCornerShape(20.dp))
                    .border(2.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0..2) {
                                val miniBoardIdx = row * 3 + col
                                val isRequiredBoard = roomState.nextRequiredBoard == null || roomState.nextRequiredBoard == miniBoardIdx
                                val winner = roomState.miniBoardWinners[miniBoardIdx]

                                MiniBoardView(
                                    miniBoardIndex = miniBoardIdx,
                                    cells = roomState.cells,
                                    isRequired = isRequiredBoard && winner.isEmpty(),
                                    winner = winner,
                                    activeGlowPulse = activeGlowPulse,
                                    onCellClick = { cellIdx -> onCellTapped(miniBoardIdx, cellIdx) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mini_board_$miniBoardIdx")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Commentary Ticker / Move Log
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xCC0B132B),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Mind",
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (mode == TicTacToeGameMode.AI_BOT) aiCommentary else roomState.lastMoveLog,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Victory Dialog
        if (roomState.winner.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(
                        text = if (roomState.winner == "DRAW") "STALEMATE DRAW!" else "VICTORY: PLAYER ${roomState.winner}!",
                        color = if (roomState.winner == "X") Color(0xFF00F0FF) else if (roomState.winner == "O") Color(0xFFFF007A) else Color.Yellow,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Text(
                        text = "The neural mind grid has been claimed! Total moves: ${roomState.moveCount}.",
                        color = Color.White
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { RoomSyncManager.resetTicTacToeRoom(roomCodeClean) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color.Black),
                        modifier = Modifier.testTag("play_again_tictactoe")
                    ) {
                        Text("Play Again", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onBackToCavern,
                        modifier = Modifier.testTag("exit_tictactoe")
                    ) {
                        Text("Return to Cavern")
                    }
                },
                containerColor = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
fun MiniBoardView(
    miniBoardIndex: Int,
    cells: List<String>,
    isRequired: Boolean,
    winner: String,
    activeGlowPulse: Float,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = if (winner.isNotEmpty()) Color(0xFF0F172A) else if (isRequired) Color(0xFF00F0FF).copy(alpha = 0.12f) else Color(0xFF020617),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isRequired) 2.dp else 1.dp,
            color = if (isRequired) Color(0xFF00F0FF).copy(alpha = activeGlowPulse) else Color(0xFF1E293B)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            // 3x3 Mini Grid Cells
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (r in 0..2) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (c in 0..2) {
                            val cellIdxInMini = r * 3 + c
                            val globalCellIdx = miniBoardIndex * 9 + cellIdxInMini
                            val symbol = cells[globalCellIdx]

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(Color(0xFF0D1527), shape = RoundedCornerShape(6.dp))
                                    .clickable { onCellClick(cellIdxInMini) }
                                    .testTag("cell_${miniBoardIndex}_$cellIdxInMini"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (symbol.isNotEmpty()) {
                                    Text(
                                        text = symbol,
                                        color = if (symbol == "X") Color(0xFF00F0FF) else Color(0xFFFF007A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Overlay if Mini Board is Won
            if (winner.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xDD030712),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = winner,
                            color = if (winner == "X") Color(0xFF00F0FF) else if (winner == "O") Color(0xFFFF007A) else Color.Gray,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
