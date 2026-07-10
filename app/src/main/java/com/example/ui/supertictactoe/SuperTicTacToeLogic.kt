package com.example.ui.supertictactoe

object SuperTicTacToeLogic {
    // 8 winning lines in a 3x3 grid
    val WINNING_LINES = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
        listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
    )

    fun check3x3Winner(grid: List<String>): Pair<String, List<Int>?> {
        for (line in WINNING_LINES) {
            val a = grid[line[0]]
            val b = grid[line[1]]
            val c = grid[line[2]]
            if (a.isNotEmpty() && a == b && b == c) {
                return Pair(a, line)
            }
        }
        if (grid.all { it.isNotEmpty() }) {
            return Pair("DRAW", null)
        }
        return Pair("", null)
    }

    // AI Bot Move Logic
    fun calculateAiMove(
        cells: List<String>,
        miniBoardWinners: List<String>,
        nextRequiredBoard: Int?,
        aiLevel: String
    ): Pair<Int, Int>? { // Pair(miniBoardIndex, cellIndexInMini)
        // Determine available mini boards
        val availableBoards = if (nextRequiredBoard != null && miniBoardWinners[nextRequiredBoard].isEmpty()) {
            listOf(nextRequiredBoard)
        } else {
            miniBoardWinners.indices.filter { miniBoardWinners[it].isEmpty() }
        }

        if (availableBoards.isEmpty()) return null

        val candidateMoves = mutableListOf<Pair<Int, Int>>()
        for (boardIdx in availableBoards) {
            val startIndex = boardIdx * 9
            for (c in 0 until 9) {
                if (cells[startIndex + c].isEmpty()) {
                    candidateMoves.add(Pair(boardIdx, c))
                }
            }
        }

        if (candidateMoves.isEmpty()) return null

        // Easy / Novice: Random move
        if (aiLevel == "Novice") {
            return candidateMoves.random()
        }

        // Master / Cyber-Mind Gemini: Heuristic move
        // 1. Check if AI ("O") can win a mini board in 1 move
        for (move in candidateMoves) {
            val (bIdx, cIdx) = move
            val startIndex = bIdx * 9
            val tempMini = (0 until 9).map { idx ->
                if (idx == cIdx) "O" else cells[startIndex + idx]
            }
            if (check3x3Winner(tempMini).first == "O") {
                return move // Priority 1: Take mini-board win!
            }
        }

        // 2. Block human ("X") from winning a mini board
        for (move in candidateMoves) {
            val (bIdx, cIdx) = move
            val startIndex = bIdx * 9
            val tempMini = (0 until 9).map { idx ->
                if (idx == cIdx) "X" else cells[startIndex + idx]
            }
            if (check3x3Winner(tempMini).first == "X") {
                return move // Priority 2: Block human mini win!
            }
        }

        // 3. Prefer center cell (4) or corners (0, 2, 6, 8) inside mini board
        val centerMoves = candidateMoves.filter { it.second == 4 }
        if (centerMoves.isNotEmpty()) return centerMoves.random()

        val cornerMoves = candidateMoves.filter { it.second in listOf(0, 2, 6, 8) }
        if (cornerMoves.isNotEmpty()) return cornerMoves.random()

        return candidateMoves.random()
    }
}
