package id.usecase.word_battle.ui.screens.lobby

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import id.usecase.word_battle.ui.components.CountdownTimer
import kotlinx.coroutines.delay

/**
 * Dialog for waiting room before game starts
 */
@Composable
fun WaitingRoomDialog(
    gameId: String,
    playerCount: Int,
    maxPlayers: Int,
    onCancel: () -> Unit,
    onGameStarting: () -> Unit
) {
    var isStarting by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(5) }

    // Simulate game starting after some players join
    LaunchedEffect(playerCount) {
        if (playerCount >= maxPlayers) {
            delay(1000)
            isStarting = true
        }
    }

    // When countdown finishes, start the game
    LaunchedEffect(isStarting) {
        if (isStarting) {
            delay(5000)
            onGameStarting()
        }
    }

    Dialog(onDismissRequest = { /* Prevent dismiss */ }) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                // Close button
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isStarting) "Game Starting!" else "Waiting for Players",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isStarting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Game ID
                    Text(
                        text = "Game ID: $gameId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Player indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(maxPlayers) { index ->
                            val isConnected = index < playerCount
                            PlayerIndicator(isConnected = isConnected)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Player count text
                    Text(
                        text = "$playerCount/$maxPlayers players connected",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress indicator or countdown
                    AnimatedVisibility(
                        visible = !isStarting,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Waiting for more players to join...",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isStarting,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CountdownTimer(
                                seconds = 5,
                                onFinished = { /* handled by LaunchedEffect */ },
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Get ready to play!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Indicator to show player connection status
 */
@Composable
private fun PlayerIndicator(isConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse alpha"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                color = if (isConnected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .alpha(if (isConnected) 1f else alpha),
        contentAlignment = Alignment.Center
    ) {
        if (isConnected) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}