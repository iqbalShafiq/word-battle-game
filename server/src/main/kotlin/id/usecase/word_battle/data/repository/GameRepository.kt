package id.usecase.word_battle.data.repository

import id.usecase.word_battle.data.models.game.GameMode
import id.usecase.word_battle.data.models.game.GameRound
import id.usecase.word_battle.data.models.game.GameSession
import id.usecase.word_battle.data.models.game.WordSubmission

/**
 * Repository for game-related database operations
 */
interface GameRepository {
    /**
     * Create a new game session
     */
    suspend fun createGameSession(playerIds: List<String>, gameMode: GameMode): GameSession?

    /**
     * Get game session by ID
     */
    suspend fun getGameSession(id: String): GameSession?

    /**
     * Get active game sessions
     */
    suspend fun getActiveGameSessions(limit: Int = 20, offset: Int = 0): List<GameSession>

    /**
     * End game session and set winner
     */
    suspend fun endGameSession(id: String, winnerId: String?): Boolean

    /**
     * Create a new game round
     */
    suspend fun createGameRound(
        gameSessionId: String,
        roundNumber: Int,
        letters: String
    ): GameRound?

    /**
     * Add word submission to a round
     */
    suspend fun addWordSubmission(roundId: String, submission: WordSubmission): Boolean

    /**
     * Get rounds for a game session
     */
    suspend fun getRoundsForGameSession(gameSessionId: String): List<GameRound>
}