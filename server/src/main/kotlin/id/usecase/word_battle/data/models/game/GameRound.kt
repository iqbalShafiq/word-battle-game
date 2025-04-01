package id.usecase.word_battle.data.models.game

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single round in a game
 */
@Serializable
data class GameRound(
    val id: String = UUID.randomUUID().toString(),
    val gameSessionId: String,
    val roundNumber: Int,
    val letters: String,
    val submissions: List<WordSubmission> = emptyList(),
    val roundStats: RoundStats = RoundStats()
)