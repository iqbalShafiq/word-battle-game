package id.usecase.word_battle.ui.screens.lobby

import androidx.lifecycle.viewModelScope
import id.usecase.word_battle.domain.repository.AuthRepository
import id.usecase.word_battle.domain.repository.GameRepository
import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.mvi.MviViewModel
import id.usecase.word_battle.network.ConnectionStatus
import id.usecase.word_battle.network.GameWebSocketClient
import id.usecase.word_battle.protocol.GameStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Lobby screen state
 */
data class LobbyState(
    val isSearching: Boolean = false,
    val searchTimeSeconds: Int = 0,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val playerId: String = "",
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
 * Lobby screen ViewModel with WebSocket status monitoring
 */
class LobbyViewModel(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository,
    private val webSocketClient: GameWebSocketClient
) : MviViewModel<LobbyIntent, LobbyState, LobbyEffect>(LobbyState()) {

    private var searchTimerJob: Job? = null
    private var wsConnectionJob: Job? = null
    private var wsGameRoomJob: Job? = null

    init {
        // Get current user
        viewModelScope.launch {
            authRepository.getCurrentUser()?.let { user ->
                updateState { copy(playerId = user.id) }
            }
        }

        // Monitor WebSocket connection status
        wsConnectionJob = viewModelScope.launch {
            webSocketClient.connectionStatus.collectLatest { status ->
                updateState { copy(connectionStatus = status) }
            }
        }

        // Monitor game room updates
        wsGameRoomJob = viewModelScope.launch {
            gameRepository.observeGameRoom().collectLatest { gameRoom ->
                if (gameRoom.state == GameStatus.GAME_CREATED) {
                    sendEffect(LobbyEffect.GameFound(gameRoom.id))
                }
            }
        }
    }

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

        // If WebSocket not connected, connect it first
        if (state.value.connectionStatus != ConnectionStatus.CONNECTED) {
            try {
                webSocketClient.connect()
            } catch (_: Exception) {
                sendEffect(LobbyEffect.ShowError("Failed to connect to game server"))
                return
            }
        }

        updateState { copy(isSearching = true, searchTimeSeconds = 0) }

        // Start a timer to track search time
        searchTimerJob?.cancel()
        searchTimerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                delay(1_000)
                seconds++
                updateState { copy(searchTimeSeconds = seconds) }
            }
        }

        try {
            gameRepository.joinMatchmaking(
                playerId = state.value.playerId,
                gameMode = GameMode.CLASSIC
            )

//            result.onSuccess { gameId ->
//                searchTimerJob?.cancel()
//                sendEffect(LobbyEffect.GameFound(gameId))
//            }.onFailure { error ->
//                updateState { copy(isSearching = false) }
//                sendEffect(LobbyEffect.ShowError(error.message ?: "Failed to find match"))
//            }
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
            gameRepository.cancelMatchmaking(playerId = state.value.playerId)
            updateState { copy(isSearching = false, searchTimeSeconds = 0) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchTimerJob?.cancel()
        wsConnectionJob?.cancel()
        viewModelScope.launch {
            if (state.value.isSearching) {
                gameRepository.cancelMatchmaking(playerId = state.value.playerId)
            }
        }
    }
}