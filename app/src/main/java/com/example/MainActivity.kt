package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.engine3d.AppDestination
import com.example.engine3d.IntroSequenceView
import com.example.engine3d.MadLibsGameMode
import com.example.engine3d.MindCavernView
import com.example.engine3d.TicTacToeGameMode
import com.example.ui.archives.ArchivesScreen
import com.example.ui.comingsoon.ComingSoonScreen
import com.example.ui.madlibs.MadLibsScreen
import com.example.ui.supertictactoe.SuperTicTacToeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MindRealmApp()
            }
        }
    }
}

@Composable
fun MindRealmApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { GameRepository(database.gameDao()) }

    var currentDestination by remember { mutableStateOf(AppDestination.CAVERN_HUB) }
    var playIntroFirst by remember { mutableStateOf(true) }

    // Selected TicTacToe parameters
    var ticTacToeMode by remember { mutableStateOf(TicTacToeGameMode.AI_BOT) }
    var ticTacToeRoomCode by remember { mutableStateOf("") }
    var ticTacToeAiLevel by remember { mutableStateOf("Cyber-Mind Gemini AI") }

    // Selected MadLibs parameters
    var madLibsMode by remember { mutableStateOf(MadLibsGameMode.SOLO_3D) }
    var madLibsPassword by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (playIntroFirst) {
            IntroSequenceView(
                onIntroFinished = { playIntroFirst = false },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "app_destination_content",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { destination ->
                when (destination) {
                    AppDestination.CAVERN_HUB -> {
                        MindCavernView(
                            onLaunchTicTacToe = { mode, code, aiLevel ->
                                ticTacToeMode = mode
                                ticTacToeRoomCode = code
                                ticTacToeAiLevel = aiLevel
                                currentDestination = AppDestination.SUPER_TIC_TAC_TOE
                            },
                            onLaunchMadLibs = { mode, password ->
                                madLibsMode = mode
                                madLibsPassword = password
                                currentDestination = AppDestination.MADLIBS
                            },
                            onLaunchComingSoon = {
                                currentDestination = AppDestination.COMING_SOON
                            },
                            onLaunchArchives = {
                                currentDestination = AppDestination.ARCHIVES
                            },
                            onReplayIntro = {
                                playIntroFirst = true
                            }
                        )
                    }

                    AppDestination.SUPER_TIC_TAC_TOE -> {
                        SuperTicTacToeScreen(
                            mode = ticTacToeMode,
                            roomCode = ticTacToeRoomCode,
                            aiLevel = ticTacToeAiLevel,
                            repository = repository,
                            onBackToCavern = { currentDestination = AppDestination.CAVERN_HUB }
                        )
                    }

                    AppDestination.MADLIBS -> {
                        MadLibsScreen(
                            mode = madLibsMode,
                            roomPassword = madLibsPassword,
                            repository = repository,
                            onBackToCavern = { currentDestination = AppDestination.CAVERN_HUB }
                        )
                    }

                    AppDestination.COMING_SOON -> {
                        ComingSoonScreen(
                            onBackToCavern = { currentDestination = AppDestination.CAVERN_HUB }
                        )
                    }

                    AppDestination.ARCHIVES -> {
                        ArchivesScreen(
                            repository = repository,
                            onBackToCavern = { currentDestination = AppDestination.CAVERN_HUB }
                        )
                    }
                }
            }
        }
    }
}
