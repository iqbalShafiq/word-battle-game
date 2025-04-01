package id.usecase.word_battle.data.models.player

import kotlinx.serialization.Serializable

/**
 * Player statistics
 */
@Serializable
data class PlayerStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalScore: Int = 0,
    val highestWordScore: Int = 0,
    val wordsFound: Int = 0,
    val averageScorePerGame: Double = 0.0
)
