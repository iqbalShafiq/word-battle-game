package id.usecase.word_battle.ui.screens.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.word_battle.ui.components.AccentCard
import id.usecase.word_battle.ui.components.BorderedButton
import id.usecase.word_battle.ui.components.GameCard
import id.usecase.word_battle.ui.components.LetterCard
import id.usecase.word_battle.ui.components.PasswordTextField
import id.usecase.word_battle.ui.components.PlayerScore
import id.usecase.word_battle.ui.components.PrimaryButton
import id.usecase.word_battle.ui.components.SecondaryButton
import id.usecase.word_battle.ui.components.StandardCard
import id.usecase.word_battle.ui.components.StandardTextField
import id.usecase.word_battle.ui.components.TextActionButton
import id.usecase.word_battle.ui.components.TimerBar
import id.usecase.word_battle.ui.components.WordDisplay
import id.usecase.word_battle.ui.components.WordInputField
import id.usecase.word_battle.ui.theme.WordBattleTheme

/**
 * Component demo screen to showcase all UI components
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentsDemoScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var wordInput by remember { mutableStateOf("") }
    var timeRemaining by remember { mutableStateOf(30) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("UI Components") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Buttons",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryButton(
                    text = "Play",
                    onClick = {},
                    icon = Icons.Rounded.PlayArrow,
                    modifier = Modifier.weight(1f)
                )

                SecondaryButton(
                    text = "Settings",
                    onClick = {},
                    icon = Icons.Rounded.Settings,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BorderedButton(
                    text = "Rules",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                TextActionButton(
                    text = "Skip Tutorial",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Input Fields",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            StandardTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                leadingIcon = Icons.Filled.Person,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(8.dp))

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Game UI Components",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            AccentCard(
                title = "Round 3",
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Create a word using these letters",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LetterCard(letter = 'W')
                    LetterCard(letter = 'O')
                    LetterCard(letter = 'R', isHighlighted = true)
                    LetterCard(letter = 'D')
                    LetterCard(letter = 'S')
                }

                Spacer(modifier = Modifier.height(16.dp))

                TimerBar(
                    durationSeconds = 60,
                    currentTimeSeconds = timeRemaining,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StandardCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Players",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                PlayerScore(
                    username = "You",
                    score = 120,
                    isCurrentPlayer = true,
                    isCurrentTurn = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                PlayerScore(
                    username = "Opponent",
                    score = 90,
                    isCurrentPlayer = false,
                    isCurrentTurn = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StandardCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Your Words",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                WordDisplay(
                    word = "BATTLE",
                    points = 12,
                    isValidWord = true,
                    modifier = Modifier.fillMaxWidth()
                )

                WordDisplay(
                    word = "WORD",
                    points = 8,
                    isValidWord = true,
                    modifier = Modifier.fillMaxWidth()
                )

                WordDisplay(
                    word = "QWXYZ",
                    points = 0,
                    isValidWord = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Word Input",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            WordInputField(
                value = wordInput,
                onValueChange = { wordInput = it },
                maxLength = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = "Submit Word",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview
@Composable
fun ComponentsDemoPreview() {
    WordBattleTheme {
        ComponentsDemoScreen()
    }
}