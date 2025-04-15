package id.usecase.word_battle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Timer bar that animates counting down
 */
@Composable
fun TimerBar(
    durationSeconds: Int,
    currentTimeSeconds: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(1f) }

    // Animate the timer bar decreasing
    LaunchedEffect(currentTimeSeconds) {
        animatedProgress.snapTo(currentTimeSeconds.toFloat() / durationSeconds.toFloat())

        if (currentTimeSeconds > 0) {
            animatedProgress.animateTo(
                targetValue = (currentTimeSeconds - 1f) / durationSeconds.toFloat(),
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress.value)
                .height(10.dp)
                .background(
                    when {
                        animatedProgress.value > 0.6f -> MaterialTheme.colorScheme.primary
                        animatedProgress.value > 0.3f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
        )
    }

    Text(
        text = "$currentTimeSeconds s",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

/**
 * Player score display component
 */
@Composable
fun PlayerScore(
    username: String,
    score: Int,
    isCurrentPlayer: Boolean = false,
    isCurrentTurn: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCurrentTurn -> MaterialTheme.colorScheme.primaryContainer
        isCurrentPlayer -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (isCurrentTurn) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = username,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Countdown animation for round start
 */
@Composable
fun CountdownTimer(
    seconds: Int,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingSeconds by remember { mutableIntStateOf(seconds) }

    LaunchedEffect(seconds) {
        remainingSeconds = seconds
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        }
        onFinished()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = remainingSeconds.toString(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Word display component to show guessed words
 */
@Composable
fun WordDisplay(
    word: String,
    points: Int,
    isValidWord: Boolean = true,
    modifier: Modifier = Modifier
) {
    val textColor = if (isValidWord) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isValidWord) {
            Text(
                text = "+$points",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
        } else {
            Text(
                text = "invalid",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}