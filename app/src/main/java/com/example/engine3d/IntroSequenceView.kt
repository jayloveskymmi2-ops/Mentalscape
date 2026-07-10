package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class IntroStage {
    ORBITING_HEAD,
    ALIGNING_EYES,
    SINKING_INTO_EYES,
    INSIDE_CAVERN
}

@Composable
fun IntroSequenceView(
    onIntroFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember { mutableStateOf(IntroStage.ORBITING_HEAD) }
    var orbitAngleDeg by remember { mutableFloatStateOf(0f) }
    var userManualYaw by remember { mutableFloatStateOf(0f) }
    var userManualPitch by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("INITIALIZING CORTEX SCAN...") }

    val camDistAnim = remember { Animatable(600f) }
    val eyeIrisZoomAnim = remember { Animatable(0f) }
    val cavernTransitionAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "intro_particles")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Star/neural particles background
    val particles = remember {
        List(80) {
            Point3D(
                x = Random.nextFloat() * 1000f - 500f,
                y = Random.nextFloat() * 1000f - 500f,
                z = Random.nextFloat() * 1000f - 500f
            )
        }
    }

    // Auto-orbit loop in Stage 1
    LaunchedEffect(stage) {
        when (stage) {
            IntroStage.ORBITING_HEAD -> {
                statusText = "PHASE 1: 3D HEAD ORBIT SCAN"
                var angle = 0f
                while (stage == IntroStage.ORBITING_HEAD) {
                    angle = (angle + 1.2f) % 360f
                    orbitAngleDeg = angle
                    delay(16)
                }
            }
            IntroStage.ALIGNING_EYES -> {
                statusText = "PHASE 2: ALIGNING OCULAR SYNAPSES..."
                camDistAnim.animateTo(380f, tween(1000))
                delay(400)
                stage = IntroStage.SINKING_INTO_EYES
            }
            IntroStage.SINKING_INTO_EYES -> {
                statusText = "PHASE 3: SINKING INTO EYE / PUPIL..."
                eyeIrisZoomAnim.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
                camDistAnim.animateTo(80f, tween(1200))
                cavernTransitionAlpha.animateTo(1f, tween(600))
                delay(300)
                stage = IntroStage.INSIDE_CAVERN
                delay(400)
                onIntroFinished()
            }
            IntroStage.INSIDE_CAVERN -> {
                onIntroFinished()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    userManualYaw += dragAmount.x * 0.3f
                    userManualPitch += dragAmount.y * 0.3f
                }
            }
    ) {
        // 3D Canvas Rendering
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val totalYawRad = Math.toRadians((orbitAngleDeg + userManualYaw).toDouble()).toFloat()
            val totalPitchRad = Math.toRadians((userManualPitch * 0.5f).toDouble()).toFloat()

            // Draw floating space star particles
            particles.forEach { p ->
                val proj = Head3DModel.project(
                    point = p,
                    rotXRad = totalPitchRad,
                    rotYRad = totalYawRad,
                    rotZRad = 0f,
                    camDist = camDistAnim.value,
                    screenWidth = width,
                    screenHeight = height
                )
                drawCircle(
                    color = Color(0xFF00F0FF).copy(alpha = 0.5f),
                    radius = 2.5f,
                    center = proj
                )
            }

            // Draw Head Wireframe Mesh
            val lines = Head3DModel.headLines
            lines.forEach { line ->
                val p1 = Head3DModel.project(
                    point = line.p1,
                    rotXRad = totalPitchRad,
                    rotYRad = totalYawRad,
                    rotZRad = 0f,
                    camDist = camDistAnim.value,
                    screenWidth = width,
                    screenHeight = height
                )
                val p2 = Head3DModel.project(
                    point = line.p2,
                    rotXRad = totalPitchRad,
                    rotYRad = totalYawRad,
                    rotZRad = 0f,
                    camDist = camDistAnim.value,
                    screenWidth = width,
                    screenHeight = height
                )

                drawLine(
                    color = line.color.copy(alpha = pulseGlow * 0.85f),
                    start = p1,
                    end = p2,
                    strokeWidth = line.strokeWidth * (if (stage == IntroStage.SINKING_INTO_EYES) 1.8f else 1.2f),
                    cap = StrokeCap.Round
                )
            }

            // Eye Zoom Iris Warp Overlay during Stage 2 & 3
            if (eyeIrisZoomAnim.value > 0f) {
                val irisRadius = (width * 0.9f) * eyeIrisZoomAnim.value
                val centerOffset = Offset(width / 2f, height / 2f)

                // Expanding pupil rings
                drawCircle(
                    color = Color(0xFF00F0FF).copy(alpha = (1f - eyeIrisZoomAnim.value).coerceAtLeast(0f)),
                    radius = irisRadius,
                    center = centerOffset
                )
                drawCircle(
                    color = Color(0xFFFF007A).copy(alpha = (0.8f * eyeIrisZoomAnim.value)),
                    radius = irisRadius * 0.6f,
                    center = centerOffset
                )
            }
        }

        // Top HUD Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            color = Color(0xCC0B132B),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Mind Icon",
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MIND REALM 3D",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = statusText,
                        color = Color(0xFF00F0FF),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (pulseGlow > 0.6f) Color(0xFF00FFCC) else Color(0xFFFF007A))
                )
            }
        }

        // Bottom Controls Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (stage) {
                    IntroStage.ORBITING_HEAD -> "Drag to tilt 3D head in space"
                    IntroStage.ALIGNING_EYES -> "Locking target coordinates..."
                    IntroStage.SINKING_INTO_EYES -> "Sinking into ocular mind matrix..."
                    IntroStage.INSIDE_CAVERN -> "Entering Mind Cavern..."
                },
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onIntroFinished() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("skip_intro_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00F0FF)
                    )
                ) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Skip")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Skip Intro")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (stage == IntroStage.ORBITING_HEAD) {
                            stage = IntroStage.ALIGNING_EYES
                        } else {
                            onIntroFinished()
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("sink_into_mind_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00F0FF),
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = if (stage == IntroStage.ORBITING_HEAD) Icons.Default.Visibility else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Sink Into Mind"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (stage == IntroStage.ORBITING_HEAD) "Sink Into Eyes" else "Enter Cavern",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
