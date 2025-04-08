package id.usecase.word_battle.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Basic alert dialog for game information and decisions
 */
@Composable
fun AlertDialog(
    title: String,
    message: String,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    icon: ImageVector = Icons.Default.Info
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when(icon) {
                        Icons.Default.Warning -> MaterialTheme.colorScheme.error
                        Icons.Default.CheckCircle -> MaterialTheme.colorScheme.primary
                        Icons.Default.Cancel -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton()

                    Spacer(modifier = Modifier.width(16.dp))

                    confirmButton()
                }
            }
        }
    }
}

/**
 * Game result dialog to show after game completes
 */
@Composable
fun GameResultDialog(
    isWinner: Boolean,
    score: Int,
    opponentScore: Int,
    onPlayAgain: () -> Unit,
    onBackToLobby: () -> Unit
) {
    Dialog(onDismissRequest = {}) { // Prevent dismissal with outside click
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = if (isWinner) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isWinner) "You Won!" else "You Lost!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Final Score",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$score - $opponentScore",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "Play Again",
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                SecondaryButton(
                    text = "Back to Lobby",
                    onClick = onBackToLobby,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}