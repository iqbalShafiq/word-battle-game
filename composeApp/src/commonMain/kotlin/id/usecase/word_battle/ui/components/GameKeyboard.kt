package id.usecase.word_battle.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * On-screen keyboard optimized for word game
 */
@Composable
fun GameKeyboard(
    availableLetters: List<Char>,
    onKeyPressed: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val rows = listOf(
        "qwertyuiop".toList(),
        "asdfghjkl".toList(),
        "zxcvbnm".toList()
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(4.dp)
    ) {
        rows.forEach { rowChars ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (rowChars.size < rows[0].size) {
                    // Center shorter rows
                    Spacer(modifier = Modifier.weight(0.25f))
                }

                rowChars.forEach { char ->
                    val isAvailable = availableLetters.contains(char)
                    KeyboardKey(
                        text = char.toString(),
                        onClick = {
                            if (isAvailable) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onKeyPressed(char)
                            }
                        },
                        isEnabled = isAvailable,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowChars.size < rows[0].size) {
                    Spacer(modifier = Modifier.weight(0.25f))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Bottom row with special keys
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Backspace key
            KeyboardKey(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBackspace()
                },
                modifier = Modifier.weight(1.5f),
                content = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        modifier = Modifier.size(24.dp)
                    )
                }
            )

            // Submit key
            KeyboardKey(
                text = "Submit",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSubmit()
                },
                modifier = Modifier.weight(3f)
            )
        }
    }
}

/**
 * Individual keyboard key
 */
@Composable
fun KeyboardKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    isEnabled: Boolean = true,
    content: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        enabled = isEnabled,
        shape = MaterialTheme.shapes.small,
        color = if (isEnabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        contentColor = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(
            alpha = 0.38f
        ),
        modifier = modifier.height(48.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            if (content != null) {
                content()
            } else if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}