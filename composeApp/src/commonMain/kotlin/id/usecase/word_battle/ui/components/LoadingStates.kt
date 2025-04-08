package id.usecase.word_battle.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Full screen loading state
 */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Loading dialog that blocks UI interaction
 */
@Composable
fun LoadingDialog(
    message: String = "Loading...",
    isVisible: Boolean = true,
    onDismissRequest: () -> Unit = {}
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

/**
 * Error state with retry option
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Try Again",
                onClick = onRetry
            )
        }
    }
}

/**
 * Searching for opponents animation
 */
@Composable
fun SearchingAnimation(
    modifier: Modifier = Modifier,
    message: String = "Looking for opponents..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(64.dp)
                .alpha(alpha)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )

        // Animated dots
        Row {
            for (i in 0 until 3) {
                val dotAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(750, easing = LinearEasing, delayMillis = i * 250),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot$i"
                )

                Text(
                    text = ".",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.alpha(dotAlpha)
                )
            }
        }
    }
}