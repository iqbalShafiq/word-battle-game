package id.usecase.word_battle.domain.repository

import id.usecase.word_battle.domain.model.Chat
import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.models.GameRoom
import id.usecase.word_battle.models.Lobby
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
    suspend fun leaveGame(playerId: String, gameId: String? = null)

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
     * End current round
     */
    fun observeErrorMessage(): Flow<String>

    /**
     * Observe lobby state changes
     */
    fun observeLobby(): Flow<Lobby?>

    /**
     * Observe game state changes
     */
    fun observeGameRoom(): Flow<GameRoom?>

    /**
     * Observe chat messages in current game
     */
    fun observeChatRoom(): Flow<List<Chat>>
}