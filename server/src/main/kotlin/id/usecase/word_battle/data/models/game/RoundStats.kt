package id.usecase.word_battle.data.models.game

import kotlinx.serialization.Serializable

/**
 * Statistics for a single round
 */
@Serializable
data class RoundStats(
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val totalWords: Int = 0,
    val validWords: Int = 0,
    val highestScore: Int = 0,
    val highestScoringPlayer: String? = null,
    val highestScoringWord: String? = null
)
