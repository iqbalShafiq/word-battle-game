package id.usecase.word_battle.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.word_battle.protocol.GameStatus
import id.usecase.word_battle.ui.components.CountdownTimer
import id.usecase.word_battle.ui.components.ErrorState
import id.usecase.word_battle.ui.components.FullScreenLoading
import id.usecase.word_battle.ui.components.GameKeyboard
import id.usecase.word_battle.ui.components.GameResultDialog
import id.usecase.word_battle.ui.components.LetterCard
import id.usecase.word_battle.ui.components.StandardCard
import id.usecase.word_battle.ui.components.TimerBar
import id.usecase.word_battle.ui.components.WordDisplay
import id.usecase.word_battle.ui.theme.WordBattleTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameId: String,
    onGameFinished: () -> Unit,
    viewModel: GameViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    // Display game result dialog when game is finished
    var showGameResultDialog by remember { mutableStateOf(false) }

    // Handle side effects
    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is GameEffect.GameFinished -> {
                    showGameResultDialog = true
                }

                is GameEffect.WordSubmitted -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarHostState.showSnackbar("Word submitted!")
                }

                is GameEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                is GameEffect.LeftGame -> {
                    onGameFinished()
                }
            }
        }
    }

    // Join game when screen is first displayed
    LaunchedEffect(key1 = gameId) {
        viewModel.processIntent(GameIntent.JoinGame)
    }

    // Handle round transitions
    LaunchedEffect(key1 = state.gameState) {
        when (state.gameState) {
            GameStatus.ROUND_OVER -> {
                // Play round end sound
                // You could add a sound player here
            }

            GameStatus.GAME_OVER -> {
                showGameResultDialog = true
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Round ${state.currentRound}/${state.maxRounds}")
                },
                actions = {
                    // Exit button - consider handling game abandonment
                    IconButton(onClick = { viewModel.processIntent(GameIntent.LeaveGame) }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    FullScreenLoading(message = "Loading game...")
                }

                state.errorMessage != null -> {
                    ErrorState(
                        message = state.errorMessage ?: "Something went wrong",
                        onRetry = { viewModel.processIntent(GameIntent.JoinGame) }
                    )
                }

                else -> {
                    GameContent(
                        state = state,
                        onWordChange = { word ->
                            viewModel.processIntent(
                                GameIntent.UpdateCurrentWord(
                                    word
                                )
                            )
                        },
                        onSubmitWord = { viewModel.processIntent(GameIntent.SubmitWord) }
                    )
                }
            }

            // Round start countdown
            if (state.gameState == GameStatus.GAME_CREATED) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Get Ready!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CountdownTimer(
                            seconds = 3,
                            onFinished = { /* Round will start automatically */ },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Game result dialog
            if (showGameResultDialog) {
                val currentPlayer = state.players.find { it.id == state.playerId }
                val opponent = state.players.find { it.id != state.playerId }
                val isWinner =
                    currentPlayer != null && opponent != null && currentPlayer.score > opponent.score

                GameResultDialog(
                    isWinner = isWinner,
                    score = currentPlayer?.score ?: 0,
                    opponentScore = opponent?.score ?: 0,
                    onPlayAgain = {
                        showGameResultDialog = false
                        // In a real app, start a new game
                        onGameFinished()
                    },
                    onBackToLobby = onGameFinished
                )
            }
        }
    }
}

@Composable
private fun GameContent(
    state: GameUiState,
    onWordChange: (String) -> Unit,
    onSubmitWord: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Player scores
        StandardCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Scores",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.players.forEach { player ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = if (player.id == state.playerId) "${player.username} (You)" else player.username,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = player.score.toString(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Timer
        if (state.gameState == GameStatus.ROUND_ACTIVE) {
            TimerBar(
                durationSeconds = state.roundTimeSeconds,
                currentTimeSeconds = state.roundTimeRemaining,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Letters
        AnimatedVisibility(
            visible = state.gameState == GameStatus.ROUND_ACTIVE,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(durationMillis = 500)
            ),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Create words using these letters:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    state.roundLetters.forEach { letter ->
                        LetterCard(
                            letter = letter,
                            modifier = Modifier.weight(1f),
                            isActive = state.currentWord.contains(letter, ignoreCase = true)
                        )
                    }
                }
            }
        }

        // Word input
        if (state.gameState == GameStatus.ROUND_ACTIVE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Current word display
                Text(
                    text = state.currentWord.ifEmpty { "Type a word" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (state.currentWord.isEmpty())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                // On screen keyboard
                GameKeyboard(
                    availableLetters = state.roundLetters,
                    onKeyPressed = { char ->
                        onWordChange(state.currentWord + char)
                    },
                    onBackspace = {
                        if (state.currentWord.isNotEmpty()) {
                            onWordChange(state.currentWord.dropLast(1))
                        }
                    },
                    onSubmit = {
                        if (state.currentWord.length >= 3) {
                            onSubmitWord()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Submitted words
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                if (state.submittedWords.isNotEmpty()) {
                    Text(
                        text = "Your Words",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(state.submittedWords) { word ->
                WordDisplay(
                    word = word,
                    points = word.length * 2, // Simplified scoring
                    isValidWord = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
private fun GameScreenPrev() {
    WordBattleTheme {
        GameScreen(
            gameId = "gameId",
            onGameFinished = {}
        )
    }
}