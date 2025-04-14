package id.usecase.word_battle.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Network status bar that shows when connection is lost
 */
@Composable
fun NetworkStatusBar(
    isConnected: Boolean,
    isWebSocketConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val isFullyConnected = isConnected && isWebSocketConnected

    AnimatedVisibility(
        visible = !isFullyConnected,
        enter = expandVertically(
            animationSpec = tween(300, easing = LinearOutSlowInEasing)
        ) + fadeIn(),
        exit = shrinkVertically(
            animationSpec = tween(300, easing = LinearOutSlowInEasing)
        ) + fadeOut(),
        modifier = modifier
    ) {
        val message = when {
            !isConnected -> "No internet connection"
            !isWebSocketConnected -> "Reconnecting to game server..."
            else -> ""
        }

        val backgroundColor = when {
            !isConnected -> MaterialTheme.colorScheme.error
            !isWebSocketConnected -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 40.dp, bottom = 8.dp, start = 16.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (!isConnected) Icons.Rounded.CloudOff else Icons.Rounded.Wifi,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 4.dp)
                )

                Text(
                    text = message,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}