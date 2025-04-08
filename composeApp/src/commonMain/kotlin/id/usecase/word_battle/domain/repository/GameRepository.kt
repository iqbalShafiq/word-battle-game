package id.usecase.word_battle.domain.repository

import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.models.GameRoom
import id.usecase.word_battle.models.GameState
import kotlinx.coroutines.flow.Flow

/**
 * Repository for game operations
 */
interface GameRepository {
    /**
     * Enter matchmaking queue
     */
    suspend fun findMatch(): Result<String>

    /**
     * Cancel matchmaking
     */
    suspend fun cancelMatchmaking()

    /**
     * Join a game by ID
     */
    suspend fun joinGame(gameId: String): Result<GameRoom>

    /**
     * Leave current game
     */
    suspend fun leaveGame()

    /**
     * Submit a word
     */
    suspend fun submitWord(gameId: String, word: String): Result<Int>

    /**
     * Observe game state changes
     */
    fun observeGameState(gameId: String): Flow<GameState>

    /**
     * Observe players in current game
     */
    fun observePlayers(gameId: String): Flow<List<GamePlayer>>
}