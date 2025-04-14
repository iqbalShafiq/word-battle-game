package id.usecase.word_battle.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.usecase.word_battle.protocol.GameStatus
import kotlinx.coroutines.delay

/**
 * Display round transition animation
 */
@Composable
fun RoundTransition(
    roundNumber: Int,
    maxRounds: Int,
    gameStatus: GameStatus,
    onTransitionComplete: () -> Unit = {}
) {
    var showTransition by remember(roundNumber, gameStatus) {
        mutableStateOf(gameStatus == GameStatus.ROUND_OVER || gameStatus == GameStatus.ROUND_ACTIVE)
    }

    // Hide transition after delay
    LaunchedEffect(showTransition, gameStatus, roundNumber) {
        if (showTransition) {
            delay(3000) // Show transition for 3 seconds
            showTransition = false
            onTransitionComplete()
        }
    }

    AnimatedVisibility(
        visible = showTransition,
        enter = fadeIn(animationSpec = tween(500, easing = LinearEasing)) +
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                ),
        exit = fadeOut(animationSpec = tween(500)) +
                scaleOut(targetScale = 1.2f, animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (gameStatus == GameStatus.ROUND_OVER && roundNumber < maxRounds) {
                    Text(
                        text = "Round $roundNumber Complete!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Get Ready for Round ${roundNumber + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                } else if (gameStatus == GameStatus.ROUND_ACTIVE) {
                    Text(
                        text = "Round $roundNumber",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Starting Now!",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                } else if (gameStatus == GameStatus.GAME_OVER) {
                    Text(
                        text = "Game Complete!",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}