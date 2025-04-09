package id.usecase.word_battle.data.repository

import id.usecase.word_battle.domain.repository.GameRepository
import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.models.GameRoom
import id.usecase.word_battle.models.GameState
import id.usecase.word_battle.network.GameWebSocketClient
import id.usecase.word_battle.protocol.GameCommand
import id.usecase.word_battle.protocol.GameEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Implementation of GameRepository
 * This is a mock implementation for demo purposes
 */
class GameRepositoryImpl(
    private val webSocketClient: GameWebSocketClient,
    private val scope: CoroutineScope
) : GameRepository {
    // Mock game state for demo
    private val gameState = MutableStateFlow<GameRoom?>(null)

    init {
        scope.launch {
            // Listen for game updates from WebSocket
            webSocketClient.incomingCommand
                .filter { it is GameEvent.GameCreated }
                .map {
                    val event = it as GameEvent.GameCreated
                    GameRoom(
                        id = event.gameId,
                        gamePlayers = event.players,
                        state = GameState.STARTING
                    )
                }
                .collect { game ->
                    gameState.update {
                        it?.copy(
                            id = game.id,
                            gamePlayers = game.gamePlayers,
                            state = game.state
                        )
                    }
                }
        }
    }

    override suspend fun findMatch(playerId: String, gameMode: GameMode) {
        webSocketClient.sendCommand(
            message = GameCommand.JoinQueue(
                playerId = playerId,
                gameMode = gameMode
            )
        )
    }

    override suspend fun cancelMatchmaking() {
        gameState.value = null
    }

    override suspend fun joinGame(gameId: String): Result<GameRoom> {
        // In a real app, would connect to an existing game
        val currentGame = gameState.value ?: return Result.failure(Exception("Game not found"))
        return Result.success(currentGame)
    }

    override suspend fun leaveGame() {
        gameState.value = null
    }

    override suspend fun submitWord(gameId: String, word: String): Result<Int> {
        // Mock word validation and scoring
        val points = word.length * 2

        // Update player score in game state
        val currentGame = gameState.value ?: return Result.failure(Exception("Game not found"))
        val updatedPlayers = currentGame.gamePlayers.map { player ->
            if (player.isActive) {
                player.copy(score = player.score + points)
            } else {
                player
            }
        }

        gameState.value = currentGame.copy(gamePlayers = updatedPlayers)

        return Result.success(points)
    }

    override fun observeGameState(gameId: String): Flow<GameState> {
        return gameState.map { it?.state ?: GameState.IN_PROGRESS }
    }

    override fun observePlayers(gameId: String): Flow<List<GamePlayer>> {
        return gameState.map { it?.gamePlayers ?: emptyList() }
    }
}