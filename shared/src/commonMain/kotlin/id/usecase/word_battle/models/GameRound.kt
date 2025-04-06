package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

/**
 * Represents a single round in a game (shared model)
 */
@Serializable
data class GameRound(
    val id: String,
    val gameId: String,
    val roundNumber: Int,
    val letters: String,
    val timeRemainingSeconds: Int = 60,
    val submissions: List<WordSubmission> = emptyList()
)