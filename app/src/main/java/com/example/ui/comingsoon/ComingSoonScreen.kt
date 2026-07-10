package com.example.ui.comingsoon

import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun ComingSoonScreen(
    onBackToCavern: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alphaWave by remember { mutableFloatStateOf(40f) }
    var betaWave by remember { mutableFloatStateOf(75f) }
    var thetaWave by remember { mutableFloatStateOf(20f) }

    val isResonant = abs(alphaWave - 60f) < 8f && abs(betaWave - 80f) < 8f && abs(thetaWave - 50f) < 8f

    val infiniteTransition = rememberInfiniteTransition(label = "coming_soon_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

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
                    modifier = Modifier.testTag("coming_soon_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFA855F7)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MIND EXPANSION VAULT",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "AREA MARKED AS [COMING SOON]",
                        color = Color(0xFFA855F7),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Neural Resonance Tuner Mini Game
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, if (isResonant) Color(0xFF00FFCC) else Color(0xFFA855F7).copy(alpha = pulseGlow))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Tuner",
                                tint = Color(0xFFA855F7)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MIND RESONANCE TUNER",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Surface(
                            color = if (isResonant) Color(0xFF00FFCC).copy(alpha = 0.2f) else Color(0xFFA855F7).copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (isResonant) "SYNCED!" else "TUNING...",
                                color = if (isResonant) Color(0xFF00FFCC) else Color(0xFFA855F7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Alpha Wave (Target 60Hz): ${alphaWave.toInt()}Hz", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Slider(
                        value = alphaWave,
                        onValueChange = { alphaWave = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFA855F7), activeTrackColor = Color(0xFFA855F7))
                    )

                    Text("Beta Wave (Target 80Hz): ${betaWave.toInt()}Hz", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Slider(
                        value = betaWave,
                        onValueChange = { betaWave = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00F0FF), activeTrackColor = Color(0xFF00F0FF))
                    )

                    Text("Theta Wave (Target 50Hz): ${thetaWave.toInt()}Hz", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Slider(
                        value = thetaWave,
                        onValueChange = { thetaWave = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF007A), activeTrackColor = Color(0xFFFF007A))
                    )

                    if (isResonant) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = Color(0x3300FFCC),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "EASTER EGG UNLOCKED: 'The mind is not a vessel to be filled, but a fire to be kindled.'",
                                color = Color(0xFF00FFCC),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upcoming Features Teaser Cards
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "UPCOMING NEURAL MODULES",
                        color = Color(0xFFA855F7),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                val teasers = listOf(
                    Pair("Mind Telepathy Engine", "3D Peer-to-peer thought transmission card game."),
                    Pair("Dreamscape Explorer", "Procedural 3D dream world generator."),
                    Pair("Memory Maze 3D", "Spatial memory recall challenge inside synaptic labyrinths.")
                )

                items(teasers) { (title, desc) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0B132B),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFA855F7)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = desc, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
