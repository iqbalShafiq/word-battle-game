package id.usecase.word_battle.data.models.game

import kotlinx.serialization.Serializable

/**
 * Statistics for a single round
 */
@Serializable
data class RoundStats(
    val validWordsCount: Int = 0,
    val highestScoringWord: String = "",
    val highestScore: Int = 0,
    val roundWinnerId: String? = null
)
