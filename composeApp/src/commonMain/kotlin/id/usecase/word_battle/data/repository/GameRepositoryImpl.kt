package id.usecase.word_battle.data.repository

import id.usecase.word_battle.domain.model.Game
import id.usecase.word_battle.domain.model.GameState
import id.usecase.word_battle.domain.model.Player
import id.usecase.word_battle.domain.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementation of GameRepository
 * This is a mock implementation for demo purposes
 */
class GameRepositoryImpl : GameRepository {

    // Mock game state for demo
    private val gameFlow = MutableStateFlow<Game?>(null)

    override suspend fun findMatch(): Result<String> {
        // Simulate network delay
        delay(2000)

        // Generate fake game ID
        val gameId = UUID.randomUUID().toString()

        // Create mock game
        val game = Game(
            id = gameId,
            players = listOf(
                Player(id = "player-1", username = "You", isCurrentPlayer = true),
                Player(id = "player-2", username = "Opponent")
            ),
            currentRound = 1,
            maxRounds = 3,
            state = GameState.WAITING
        )

        gameFlow.value = game

        return Result.success(gameId)
    }

    override suspend fun cancelMatchmaking() {
        gameFlow.value = null
    }

    override suspend fun joinGame(gameId: String): Result<Game> {
        // In a real app, would connect to an existing game
        val currentGame = gameFlow.value ?: return Result.failure(Exception("Game not found"))
        return Result.success(currentGame)
    }

    override suspend fun leaveGame() {
        gameFlow.value = null
    }

    override suspend fun submitWord(gameId: String, word: String): Result<Int> {
        // Mock word validation and scoring
        val points = word.length * 2

        // Update player score in game state
        val currentGame = gameFlow.value ?: return Result.failure(Exception("Game not found"))
        val updatedPlayers = currentGame.players.map { player ->
            if (player.isCurrentPlayer) {
                player.copy(score = player.score + points)
            } else {
                player
            }
        }

        gameFlow.value = currentGame.copy(players = updatedPlayers)

        return Result.success(points)
    }

    override fun observeGameState(gameId: String): Flow<GameState> {
        return gameFlow.map { it?.state ?: GameState.WAITING }
    }

    override fun observePlayers(gameId: String): Flow<List<Player>> {
        return gameFlow.map { it?.players ?: emptyList() }
    }
}