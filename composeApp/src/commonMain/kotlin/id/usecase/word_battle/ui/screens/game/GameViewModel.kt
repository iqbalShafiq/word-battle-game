package id.usecase.word_battle.ui.screens.game

import androidx.lifecycle.viewModelScope
import id.usecase.word_battle.domain.repository.AuthRepository
import id.usecase.word_battle.domain.repository.GameRepository
import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.mvi.MviViewModel
import id.usecase.word_battle.protocol.GameStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Game screen state
 */
data class GameUiState(
    val gameId: String = "",
    val playerId: String = "",
    val players: List<GamePlayer> = emptyList(),
    val gameState: GameStatus = GameStatus.ROUND_ACTIVE,
    val currentRound: Int = 1,
    val currentRoundId: String = "",
    val maxRounds: Int = 3,
    val roundLetters: List<Char> = emptyList(),
    val currentWord: String = "",
    val submittedWords: List<String> = emptyList(),
    val roundTimeSeconds: Int = 60,
    val roundTimeRemaining: Int = 60,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)

/**
 * Game screen intents
 */
sealed class GameIntent {
    object JoinGame : GameIntent()
    object LeaveGame : GameIntent()
    data class UpdateCurrentWord(val word: String) : GameIntent()
    object SubmitWord : GameIntent()
}

/**
 * Game screen effects
 */
sealed class GameEffect {
    object GameFinished : GameEffect()
    object WordSubmitted : GameEffect()
    data class ShowError(val message: String) : GameEffect()
}

/**
 * Game screen ViewModel with round management
 */
class GameViewModel(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : MviViewModel<GameIntent, GameUiState, GameEffect>(GameUiState()) {

    private var roundTimerJob: Job? = null

    init {
        // Get current user
        viewModelScope.launch {
            getCurrentUser()
            observeGameState()
        }
    }

    override suspend fun handleIntent(intent: GameIntent, state: GameUiState) {
        when (intent) {
            is GameIntent.JoinGame -> {
                joinGame()
            }

            is GameIntent.LeaveGame -> {
                try {
                    gameRepository.leaveGame(playerId = state.playerId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            is GameIntent.UpdateCurrentWord -> {
                // Only allow using available letters
                val currentLetters = state.roundLetters.map { it.lowercaseChar() }
                val newWord = intent.word.uppercase()
                val isAllowedWord = newWord.all { char ->
                    val lowercaseChar = char.lowercaseChar()
                    currentLetters.contains(lowercaseChar) &&
                            countOccurrences(newWord.lowercase(), lowercaseChar) <=
                            countOccurrences(currentLetters.joinToString(""), lowercaseChar)
                }

                if (isAllowedWord || newWord.isEmpty()) {
                    updateState { copy(currentWord = newWord) }
                }
            }

            is GameIntent.SubmitWord -> {
                submitWord()
            }
        }
    }

    private suspend fun getCurrentUser() {
        authRepository.getCurrentUser()?.let { user ->
            updateState { copy(playerId = user.id) }
        }
    }

    private suspend fun joinGame() {
        updateState { copy(isLoading = true, errorMessage = null) }

        try {
            gameRepository.joinMatchmaking(
                playerId = state.value.playerId,
                gameMode = GameMode.CLASSIC
            )
        } catch (e: Exception) {
            updateState {
                copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An error occurred"
                )
            }
            sendEffect(GameEffect.ShowError(e.message ?: "An error occurred"))
        }
    }

    private suspend fun observeGameState() {
        gameRepository.observeGameRoom()
            .catch { e ->
                updateState { copy(errorMessage = e.message) }
            }
            .collectLatest { game ->
                if (game == null) {
                    updateState { copy(isLoading = false) }
                    return@collectLatest
                }

                val oldStatus = state.value.gameState
                val newStatus = game.state

                updateState {
                    copy(
                        isLoading = game.state == GameStatus.WAITING,
                        gameId = game.id,
                        gameState = game.state,
                        players = game.gamePlayers,
                        currentRound = game.currentRound,
                        currentRoundId = game.currentRoundId,
                        maxRounds = game.maxRounds,
                        roundLetters = game.currentLetters.map { it.toChar() },
                        roundTimeRemaining = game.remainingRoundTime
                    )
                }

                // Handle status transitions
                handleStatusTransition(oldStatus, newStatus)

                // If game is finished, send effect
                if (newStatus == GameStatus.GAME_OVER) {
                    sendEffect(GameEffect.GameFinished)
                }
            }
    }

    private fun handleStatusTransition(oldStatus: GameStatus, newStatus: GameStatus) {
        // Status transition to PLAYING -> start round timer
        if (oldStatus != GameStatus.ROUND_ACTIVE && newStatus == GameStatus.ROUND_ACTIVE) {
            startRoundTimer()
        }

        // Status transition to anything else -> stop round timer
        if (oldStatus == GameStatus.ROUND_ACTIVE && newStatus != GameStatus.ROUND_ACTIVE) {
            stopRoundTimer()
        }

        // Status transition to ROUND_END -> clear current word
        if (newStatus == GameStatus.ROUND_OVER) {
            updateState { copy(currentWord = "") }
        }
    }

    private fun startRoundTimer() {
        stopRoundTimer()
        updateState { copy(roundTimeRemaining = roundTimeSeconds) }
        roundTimerJob = viewModelScope.launch {
            for (i in state.value.roundTimeSeconds downTo 1) {
                updateState { copy(roundTimeRemaining = i) }
                delay(1000)
            }
            updateState { copy(roundTimeRemaining = 0) }
        }
    }

    private fun stopRoundTimer() {
        roundTimerJob?.cancel()
        roundTimerJob = null
    }

    private suspend fun submitWord() {
        val word = state.value.currentWord.trim()

        if (word.length < 3) {
            sendEffect(GameEffect.ShowError("Word must be at least 3 letters"))
            return
        }

        try {
            gameRepository.submitWord(
                playerId = state.value.playerId,
                gameId = state.value.gameId,
                roundId = state.value.currentRoundId,
                word = word
            )
            sendEffect(GameEffect.WordSubmitted)
        } catch (e: Exception) {
            sendEffect(GameEffect.ShowError(e.message ?: "An error occurred"))
        }
    }

    private fun countOccurrences(str: String, char: Char): Int {
        return str.count { it == char }
    }

    override fun onCleared() {
        super.onCleared()
        stopRoundTimer()
        viewModelScope.launch {
            try {
                gameRepository.leaveGame(playerId = state.value.playerId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}