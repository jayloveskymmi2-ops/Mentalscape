package com.example.ui.madlibs

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.data.SavedStory
import com.example.engine3d.MadLibsGameMode
import com.example.network.MadLibsPromptItem
import com.example.network.RoomSyncManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// Pre-packaged 3D Mind MadLibs Themes
data class MadLibsTheme(
    val title: String,
    val description: String,
    val prompts: List<MadLibsPromptItem>,
    val template: (Map<Int, Pair<String, String>>) -> String
)

object MadLibsThemes {
    val cyberHeist = MadLibsTheme(
        title = "The Cyber Mind Heist",
        description = "A futuristic infiltration into a top-secret neural mainframe.",
        prompts = listOf(
            MadLibsPromptItem(0, "Sci-Fi Weapon", "A futuristic handheld weapon"),
            MadLibsPromptItem(1, "Adjective", "A word describing an extreme state"),
            MadLibsPromptItem(2, "Plural Noun", "Items stored inside a vault"),
            MadLibsPromptItem(3, "Action Verb", "An explosive movement action"),
            MadLibsPromptItem(4, "Famous AI / Person", "A legendary cyber hacker"),
            MadLibsPromptItem(5, "Sci-Fi Location", "An uncharted digital zone")
        ),
        template = { words ->
            val w0 = words[0]?.first ?: "[WEAPON]"
            val w1 = words[1]?.first ?: "[ADJECTIVE]"
            val w2 = words[2]?.first ?: "[PLURAL NOUN]"
            val w3 = words[3]?.first ?: "[VERB]"
            val w4 = words[4]?.first ?: "[HACKER]"
            val w5 = words[5]?.first ?: "[LOCATION]"
            "Deep inside the mind cavern of $w5, operative $w4 drew their glowing $w0. The atmosphere was ridiculously $w1 as hundreds of $w2 floated through the synaptic air. Suddenly, the security AI decided to $w3, causing the entire cortex to collapse into digital chaos!"
        }
    )

    val galacticCore = MadLibsTheme(
        title = "Journey to the Galactic Core",
        description = "An interstellar expedition into deep space hyperspace.",
        prompts = listOf(
            MadLibsPromptItem(0, "Alien Species", "Name of an alien creature"),
            MadLibsPromptItem(1, "Silly Sound", "An absurd noise"),
            MadLibsPromptItem(2, "Adjective", "Describing a cosmic nebula"),
            MadLibsPromptItem(3, "Type of Food", "A meal eaten in zero gravity"),
            MadLibsPromptItem(4, "Number", "A large calculation value"),
            MadLibsPromptItem(5, "Emotion", "A feeling of intense surprise")
        ),
        template = { words ->
            val w0 = words[0]?.first ?: "[ALIEN]"
            val w1 = words[1]?.first ?: "[SOUND]"
            val w2 = words[2]?.first ?: "[ADJECTIVE]"
            val w3 = words[3]?.first ?: "[FOOD]"
            val w4 = words[4]?.first ?: "[NUMBER]"
            val w5 = words[5]?.first ?: "[EMOTION]"
            "Command Log: Starship Odyssey reached the $w2 Nebula. Captain $w0 shouted '$w1!' when $w4 alien probes attacked our reserve of $w3. The crew was filled with absolute $w5, but our warp engines carried us safely into the mind void!"
        }
    )
}

@Composable
fun MadLibsScreen(
    mode: MadLibsGameMode,
    roomPassword: String,
    repository: GameRepository,
    onBackToCavern: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cleanPassword = roomPassword.ifBlank { "STORY-88" }

    var selectedTheme by remember { mutableStateOf(MadLibsThemes.cyberHeist) }
    var currentInputWord by remember { mutableStateOf("") }
    var storySaved by remember { mutableStateOf(false) }

    // TTS Setup
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val ttsObj = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = ttsObj
        onDispose {
            ttsObj.stop()
            ttsObj.shutdown()
        }
    }

    // Room Sync state
    val roomStateFlow = remember(cleanPassword, selectedTheme) {
        RoomSyncManager.getOrCreateMadLibsRoom(
            code = cleanPassword,
            asHost = true,
            storyTitle = selectedTheme.title,
            prompts = selectedTheme.prompts,
            playerName = if (mode == MadLibsGameMode.SOLO_3D) "Human Mind" else "Player 1"
        )
    }

    val roomState by roomStateFlow.collectAsState()

    // Function to submit current prompt word
    val onSubmitWord = {
        if (currentInputWord.isNotBlank()) {
            val playerLabel = if (mode == MadLibsGameMode.SOLO_3D) "Player" else "Player ${roomState.currentTurnPlayer}"
            RoomSyncManager.submitMadLibsWord(
                code = cleanPassword,
                promptIndex = roomState.currentPromptIndex,
                word = currentInputWord.trim(),
                playerName = playerLabel,
                storyTemplate = selectedTheme.template
            )
            currentInputWord = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
    ) {
        // Ambient Synth Background Lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..10) {
                val y = size.height * (i / 10f)
                drawLine(
                    color = Color(0xFFFF007A).copy(alpha = 0.15f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
        }

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
                    modifier = Modifier.testTag("madlibs_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFFF007A)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "3D INTERACTIVE MADLIBS",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (mode == MadLibsGameMode.SOLO_3D) "SOLO STORY GENERATOR" else "ROOM PASS: $cleanPassword",
                        color = Color(0xFFFF007A),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { RoomSyncManager.resetMadLibsRoom(cleanPassword, selectedTheme.prompts) },
                    modifier = Modifier.testTag("madlibs_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Story",
                        tint = Color(0xFF00F0FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Story Theme Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(MadLibsThemes.cyberHeist, MadLibsThemes.galacticCore).forEach { theme ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTheme = theme
                                RoomSyncManager.resetMadLibsRoom(cleanPassword, theme.prompts)
                            },
                        color = if (selectedTheme.title == theme.title) Color(0xFFFF007A).copy(alpha = 0.25f) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.5.dp,
                            color = if (selectedTheme.title == theme.title) Color(0xFFFF007A) else Color.Transparent
                        )
                    ) {
                        Text(
                            text = theme.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!roomState.isCompleted) {
                // ACTIVE WORD ENTRY STAGE
                val currentPrompt = roomState.prompts.getOrNull(roomState.currentPromptIndex)

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    color = Color(0xFF0B132B),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF007A).copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = Color(0xFFFF007A).copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "WORD ${roomState.currentPromptIndex + 1} / ${roomState.prompts.size}",
                                color = Color(0xFFFF007A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (mode == MadLibsGameMode.ROOM_PASSWORD) {
                            Text(
                                text = "CURRENT TURN: PLAYER ${roomState.currentTurnPlayer}",
                                color = Color(0xFF00F0FF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Text(
                            text = "Enter a: ${currentPrompt?.promptType ?: "Word"}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = currentPrompt?.description ?: "",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = currentInputWord,
                            onValueChange = { currentInputWord = it },
                            placeholder = { Text("Type word here...", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("madlibs_word_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFF007A),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onSubmitWord() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("madlibs_submit_word_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF007A),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Submit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Word to Story Stage", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                // STORY PLAYBACK STAGE (COMPLETED)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    color = Color(0xEE0B132B),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FFCC))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Sparkle",
                                tint = Color(0xFF00FFCC)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COMPLETED 3D STORY PLAYBACK",
                                color = Color(0xFF00FFCC),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            color = Color(0xFF050B14),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f))
                        ) {
                            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                                item {
                                    Text(
                                        text = roomState.finalStoryText,
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        lineHeight = 26.sp,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Controls: Text-To-Speech Narration & Save to Archives
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (tts != null) {
                                        if (isSpeaking) {
                                            tts?.stop()
                                            isSpeaking = false
                                        } else {
                                            tts?.speak(roomState.finalStoryText, TextToSpeech.QUEUE_FLUSH, null, "story_tts")
                                            isSpeaking = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("read_story_tts_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color.Black)
                            ) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Read")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isSpeaking) "Stop Narration" else "Narrate Story")
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.saveStory(
                                            SavedStory(
                                                title = selectedTheme.title,
                                                storyText = roomState.finalStoryText,
                                                themeName = selectedTheme.title,
                                                isMultiplayer = mode == MadLibsGameMode.ROOM_PASSWORD,
                                                roomCode = cleanPassword
                                            )
                                        )
                                        storySaved = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_story_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007A), contentColor = Color.White)
                            ) {
                                Icon(imageVector = if (storySaved) Icons.Default.CheckCircle else Icons.Default.Bookmark, contentDescription = "Save")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (storySaved) "Saved!" else "Save Story")
                            }
                        }
                    }
                }
            }
        }
    }
}
