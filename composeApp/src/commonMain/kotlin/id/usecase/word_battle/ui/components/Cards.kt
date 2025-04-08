package id.usecase.word_battle.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard card component for content containers
 */
@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Accent card with title for important information
 */
@Composable
fun AccentCard(
    title: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    titlePadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(titlePadding)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

/**
 * Game card for letters and words display during the game
 */
@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    borderColor: Color? = null,
    borderWidth: Dp = 2.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.clip(shape),
        shape = shape,
        shadowElevation = elevation,
        color = backgroundColor,
        contentColor = contentColor,
        border = borderColor?.let { BorderStroke(borderWidth, it) }
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Letter card for displaying individual letters
 */
@Composable
fun LetterCard(
    letter: Char,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    isHighlighted: Boolean = false
) {
    val backgroundColor = when {
        isHighlighted -> MaterialTheme.colorScheme.tertiary
        isActive -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isHighlighted -> MaterialTheme.colorScheme.onTertiary
        isActive -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    GameCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        elevation = if (isActive) 4.dp else 1.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}