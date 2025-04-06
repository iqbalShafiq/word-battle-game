package id.usecase.word_battle.data.repository

import id.usecase.word_battle.data.models.player.UserAccount

/**
 * Repository for player-related database operations
 */
interface PlayerRepository {
    /**
     * Create a new player
     */
    suspend fun createPlayer(username: String): UserAccount?

    /**
     * Get player by ID
     */
    suspend fun getPlayer(id: String): UserAccount?

    /**
     * Get player by username
     */
    suspend fun getPlayerByUsername(username: String): UserAccount?

    /**
     * Update player's last active timestamp
     */
    suspend fun updateLastActive(id: String): Boolean

    /**
     * Update player's statistics
     */
    suspend fun updateStats(
        id: String,
        gamesPlayed: Int,
        gamesWon: Int,
        totalScore: Int,
        wordsFound: Int
    ): Boolean
}