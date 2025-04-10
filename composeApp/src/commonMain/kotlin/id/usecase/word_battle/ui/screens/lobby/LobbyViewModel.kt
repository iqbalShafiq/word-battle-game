package id.usecase.word_battle.ui.screens.lobby

import androidx.lifecycle.viewModelScope
import id.usecase.word_battle.domain.repository.GameRepository
import id.usecase.word_battle.mvi.MviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Lobby screen state
 */
data class LobbyState(
    val isSearching: Boolean = false,
    val searchTimeSeconds: Int = 0
)

/**
 * Lobby screen intents
 */
sealed class LobbyIntent {
    object StartMatchmaking : LobbyIntent()
    object CancelMatchmaking : LobbyIntent()
    object NavigateBack : LobbyIntent()
}

/**
 * Lobby screen effects
 */
sealed class LobbyEffect {
    object NavigateBack : LobbyEffect()
    data class GameFound(val gameId: String) : LobbyEffect()
    data class ShowError(val message: String) : LobbyEffect()
}

/**
 * Lobby screen ViewModel
 */
class LobbyViewModel(
    private val gameRepository: GameRepository
) : MviViewModel<LobbyIntent, LobbyState, LobbyEffect>(LobbyState()) {

    private var searchTimerJob: Job? = null

    override suspend fun handleIntent(intent: LobbyIntent, state: LobbyState) {
        when (intent) {
            is LobbyIntent.StartMatchmaking -> {
                startMatchmaking()
            }

            is LobbyIntent.CancelMatchmaking -> {
                cancelMatchmaking()
            }

            is LobbyIntent.NavigateBack -> {
                cancelMatchmaking()
                sendEffect(LobbyEffect.NavigateBack)
            }
        }
    }

    private suspend fun startMatchmaking() {
        if (state.value.isSearching) return

        updateState { copy(isSearching = true, searchTimeSeconds = 0) }

        // Start a timer to track search time
        searchTimerJob?.cancel()
        searchTimerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                delay(1000)
                seconds++
                updateState { copy(searchTimeSeconds = seconds) }
            }
        }

        try {
            val result = gameRepository.joinMatchmaking()
            result.onSuccess { gameId ->
                searchTimerJob?.cancel()
                sendEffect(LobbyEffect.GameFound(gameId))
            }.onFailure { error ->
                updateState { copy(isSearching = false) }
                sendEffect(LobbyEffect.ShowError(error.message ?: "Failed to find match"))
            }
        } catch (e: Exception) {
            updateState { copy(isSearching = false) }
            sendEffect(LobbyEffect.ShowError(e.message ?: "Failed to find match"))
        }
    }

    private fun cancelMatchmaking() {
        if (!state.value.isSearching) return

        searchTimerJob?.cancel()
        searchTimerJob = null

        viewModelScope.launch {
            gameRepository.cancelMatchmaking()
            updateState { copy(isSearching = false, searchTimeSeconds = 0) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchTimerJob?.cancel()
    }
}