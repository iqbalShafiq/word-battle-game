package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

@Serializable
data class GameResult(
    val winnerId: String?,
    val playerScores: Map<String, Int>,
    val roundStats: List<RoundStats>
)