package com.example.engine3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

enum class AppDestination {
    CAVERN_HUB,
    SUPER_TIC_TAC_TOE,
    MADLIBS,
    COMING_SOON,
    ARCHIVES
}

enum class TicTacToeGameMode {
    AI_BOT,
    ONLINE_ROOM
}

enum class MadLibsGameMode {
    SOLO_3D,
    ROOM_PASSWORD
}

@Composable
fun MindCavernView(
    onLaunchTicTacToe: (mode: TicTacToeGameMode, roomCode: String, aiLevel: String) -> Unit,
    onLaunchMadLibs: (mode: MadLibsGameMode, roomPassword: String) -> Unit,
    onLaunchComingSoon: () -> Unit,
    onLaunchArchives: () -> Unit,
    onReplayIntro: () -> Unit,
    modifier: Modifier = Modifier
) {
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    var showTicTacToeDialog by remember { mutableStateOf(false) }
    var showMadLibsDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "cavern_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    panX = (panX + dragAmount.x * 0.2f).coerceIn(-40f, 40f)
                    panY = (panY + dragAmount.y * 0.2f).coerceIn(-30f, 30f)
                }
            }
    ) {
        // 3D Cavern Background Image & Synth Canvas
        Image(
            painter = painterResource(id = R.drawable.img_mind_cavern),
            contentDescription = "3D Mind Cavern Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.35f
        )

        // 3D Stalactites & Synapse Matrix Canvas Layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Glowing cavern floor grid
            for (i in -5..15) {
                val y = h * 0.7f + (i * 30f) + panY
                drawLine(
                    color = Color(0xFF00F0FF).copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.5f
                )
            }

            // Top Neural Stalactites
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w * 0.15f + panX, h * 0.25f)
                lineTo(w * 0.3f, 0f)
                lineTo(w * 0.5f + panX, h * 0.32f)
                lineTo(w * 0.7f, 0f)
                lineTo(w * 0.85f + panX, h * 0.22f)
                lineTo(w, 0f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0x9900F0FF))
                )
            )

            // Screen HUD Curved Border Lines
            drawRect(
                color = Color(0xFF00F0FF).copy(alpha = pulseAlpha * 0.4f),
                topLeft = Offset(w * 0.08f + panX, h * 0.12f + panY),
                size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.76f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }

        // Central Curved Screen Displaying What Person Sees
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cavern Header Bar (Immersive UI Style)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = Color(0xFF0C0C12).copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "NEURAL INTERFACE",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SUBJECT MIND HUD",
                                color = Color(0xFFF8FAFC),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF1E1B4B),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "v1.0.4",
                                    color = Color(0xFF818CF8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // SYNC Status Pill
                        Surface(
                            color = Color(0xFF062E35),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00F0FF))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SYNC: 98.2%",
                                    color = Color(0xFF22D3EE),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(
                            onClick = { onReplayIntro() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .testTag("replay_intro_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Replay Intro",
                                tint = Color(0xFFFF007A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Interactive Cavern Screen Hub
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = Color(0xFF08080C).copy(alpha = 0.85f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF22D3EE).copy(alpha = 0.4f))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header tag for 3D Viewport
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF43F5E))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE EXTERNAL POV SCREEN",
                                    color = Color(0xFF22D3EE),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Text(
                                text = "SWIPE TO PAN 3D",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Card 1: Super Tic-Tac-Toe
                    item {
                        CavernOptionCard(
                            title = "Super Tic-Tac-Toe",
                            subtitle = "3x3x3 Mental Grid Challenge vs Cyber AI Bot or Online Room Multiplayer.",
                            statusBadge = "AI MATRIX ACTIVE",
                            icon = Icons.Default.GridOn,
                            gradientColors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A)),
                            accentColor = Color(0xFF22D3EE),
                            testTag = "option_super_tictactoe",
                            onClick = { showTicTacToeDialog = true }
                        )
                    }

                    // Card 2: Interactive 3D MadLibs
                    item {
                        CavernOptionCard(
                            title = "3D Interactive MadLibs",
                            subtitle = "Interactive 3D story engine with Solo mode or turn-based room password play.",
                            statusBadge = "STORY SYNTH ACTIVE",
                            icon = Icons.Default.TextFields,
                            gradientColors = listOf(Color(0xFF4C0519), Color(0xFF0F172A)),
                            accentColor = Color(0xFFF43F5E),
                            testTag = "option_madlibs",
                            onClick = { showMadLibsDialog = true }
                        )
                    }

                    // Card 3: Mind Expansion Vault (Coming Soon)
                    item {
                        CavernOptionCard(
                            title = "Mind Expansion Vault",
                            subtitle = "Restricted neural chamber. Explore feature teasers and audio frequency tuner.",
                            statusBadge = "RESTRICTED SECTOR",
                            icon = Icons.Default.Lock,
                            gradientColors = listOf(Color(0xFF3B0764), Color(0xFF0F172A)),
                            accentColor = Color(0xFFC084FC),
                            testTag = "option_coming_soon",
                            onClick = { onLaunchComingSoon() }
                        )
                    }

                    // Card 4: Saved Mind Archives
                    item {
                        CavernOptionCard(
                            title = "Saved Mind Archives",
                            subtitle = "View saved MadLibs stories with TTS narration & local Tic-Tac-Toe records.",
                            statusBadge = "ROOM DB SAVED",
                            icon = Icons.Default.Book,
                            gradientColors = listOf(Color(0xFF0369A1), Color(0xFF0F172A)),
                            accentColor = Color(0xFF38BDF8),
                            testTag = "option_archives",
                            onClick = { onLaunchArchives() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Immersive UI Floating HUD Bottom Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                color = Color(0xFF0C0C12),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Atrium (Active)
                    Surface(
                        color = Color(0xFF1E1B4B),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Atrium",
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Atrium",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Synapse (Games)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showTicTacToeDialog = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Games",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Synapse",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Archives
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onLaunchArchives() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Archives",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Archives",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Dialog for Super Tic-Tac-Toe Mode Choice
        if (showTicTacToeDialog) {
            SuperTicTacToeModeDialog(
                onDismiss = { showTicTacToeDialog = false },
                onSelectMode = { mode, code, aiLevel ->
                    showTicTacToeDialog = false
                    onLaunchTicTacToe(mode, code, aiLevel)
                }
            )
        }

        // Dialog for MadLibs Mode Choice
        if (showMadLibsDialog) {
            MadLibsModeDialog(
                onDismiss = { showMadLibsDialog = false },
                onSelectMode = { mode, password ->
                    showMadLibsDialog = false
                    onLaunchMadLibs(mode, password)
                }
            )
        }
    }
}

@Composable
fun CavernOptionCard(
    title: String,
    subtitle: String,
    statusBadge: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors = gradientColors)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glow Icon Box
                Surface(
                    modifier = Modifier.size(50.dp),
                    color = accentColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = title,
                            color = Color(0xFFF8FAFC),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Surface(
                            color = accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = statusBadge,
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitle,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SuperTicTacToeModeDialog(
    onDismiss: () -> Unit,
    onSelectMode: (mode: TicTacToeGameMode, roomCode: String, aiLevel: String) -> Unit
) {
    var selectedMode by remember { mutableStateOf(TicTacToeGameMode.AI_BOT) }
    var roomCodeInput by remember { mutableStateOf("MIND-${(1000..9999).random()}") }
    var selectedAiLevel by remember { mutableStateOf("Cyber-Mind Gemini AI") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "SUPER TIC-TAC-TOE MODE",
                color = Color(0xFF00F0FF),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select your challenge mode:",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { selectedMode = TicTacToeGameMode.AI_BOT },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_ai_bot"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == TicTacToeGameMode.AI_BOT) Color(0xFF00F0FF) else Color(0xFF1E293B),
                            contentColor = if (selectedMode == TicTacToeGameMode.AI_BOT) Color.Black else Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.SmartToy, contentDescription = "AI")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("vs AI Bot")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { selectedMode = TicTacToeGameMode.ONLINE_ROOM },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mode_online_room"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == TicTacToeGameMode.ONLINE_ROOM) Color(0xFFFF007A) else Color(0xFF1E293B),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.People, contentDescription = "Room")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Room Code")
                    }
                }

                if (selectedMode == TicTacToeGameMode.AI_BOT) {
                    Text("Select AI Synapse Level:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Novice", "Master", "Cyber-Mind Gemini AI").forEach { level ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAiLevel = level },
                                color = if (selectedAiLevel == level) Color(0xFF00F0FF).copy(alpha = 0.3f) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedAiLevel == level) Color(0xFF00F0FF) else Color.Transparent)
                            ) {
                                Text(
                                    text = level,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text("Enter 4-Digit Room Code:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { roomCodeInput = it.uppercase() },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tictactoe_room_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF007A),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Text("Two players entering the same Room Code play together in real time!", color = Color(0xFF00FFCC), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectMode(selectedMode, roomCodeInput, selectedAiLevel) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color.Black),
                modifier = Modifier.testTag("start_tictactoe_button")
            ) {
                Text("Start Game", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF0F172A)
    )
}

@Composable
fun MadLibsModeDialog(
    onDismiss: () -> Unit,
    onSelectMode: (mode: MadLibsGameMode, password: String) -> Unit
) {
    var selectedMode by remember { mutableStateOf(MadLibsGameMode.SOLO_3D) }
    var passwordInput by remember { mutableStateOf("STORY-${(1000..9999).random()}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "INTERACTIVE 3D MADLIBS",
                color = Color(0xFFFF007A),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Choose story creation style:", color = Color.White, fontSize = 14.sp)

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { selectedMode = MadLibsGameMode.SOLO_3D },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("madlibs_mode_solo"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == MadLibsGameMode.SOLO_3D) Color(0xFFFF007A) else Color(0xFF1E293B),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.TextFields, contentDescription = "Solo")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Solo 3D")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { selectedMode = MadLibsGameMode.ROOM_PASSWORD },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("madlibs_mode_room"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == MadLibsGameMode.ROOM_PASSWORD) Color(0xFF00F0FF) else Color(0xFF1E293B),
                            contentColor = if (selectedMode == MadLibsGameMode.ROOM_PASSWORD) Color.Black else Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.People, contentDescription = "Multiplayer")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Room Pass")
                    }
                }

                if (selectedMode == MadLibsGameMode.ROOM_PASSWORD) {
                    Text("Enter Story Room Password:", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it.uppercase() },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("madlibs_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00F0FF),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Text("Two players take turns entering words to build the story together!", color = Color(0xFF00FFCC), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectMode(selectedMode, passwordInput) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007A), contentColor = Color.White),
                modifier = Modifier.testTag("start_madlibs_button")
            ) {
                Text("Launch Story", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF0F172A)
    )
}
