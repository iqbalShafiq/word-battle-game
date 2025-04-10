package id.usecase.word_battle.domain.repository

import id.usecase.word_battle.domain.model.Chat
import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.models.GameState
import kotlinx.coroutines.flow.Flow

/**
 * Repository for game operations
 */
interface GameRepository {
    /**
     * Enter matchmaking queue
     */
    suspend fun joinMatchmaking(playerId: String, gameMode: GameMode)

    /**
     * Cancel matchmaking
     */
    suspend fun cancelMatchmaking(playerId: String)

    /**
     * Leave current game
     */
    suspend fun leaveGame(playerId: String)

    /**
     * Submit a word
     */
    suspend fun submitWord(
        playerId: String,
        gameId: String,
        roundId: String,
        word: String
    )

    /**
     * Observe game state changes
     */
    fun observeGameState(gameId: String): Flow<GameState>

    /**
     * Observe players in current game
     */
    fun observePlayers(gameId: String): Flow<List<GamePlayer>>

    /**
     * Observe chat messages in current game
     */
    fun observeChatRoom(gameId: String): Flow<List<Chat>>
}