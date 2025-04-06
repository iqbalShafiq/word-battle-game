package id.usecase.word_battle.data.models.game

import id.usecase.word_battle.models.GameRound
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single round in a game
 */
@Serializable
data class GameRoundEntity(
    val id: String = UUID.randomUUID().toString(),
    val gameSessionId: String,
    val roundNumber: Int,
    val letters: String,
    val submissions: List<WordSubmissionEntity> = emptyList(),
    val roundStats: RoundStats = RoundStats()
)

/**
 * Extension function to convert entity to shared model
 */
fun GameRoundEntity.toSharedModel(): GameRound {
    return GameRound(
        id = this.id,
        gameId = this.gameSessionId,
        roundNumber = this.roundNumber,
        letters = this.letters,
        timeRemainingSeconds = calculateRemainingTime(),
        submissions = this.submissions.map { it.toSharedModel() }
    )
}

/**
 * Calculate remaining time in seconds based on round start time
 */
private fun GameRoundEntity.calculateRemainingTime(): Int {
    val elapsed = (System.currentTimeMillis() - this.roundStats.startTime) / 1000
    val roundDuration = 60 // 60 seconds per round

    return (roundDuration - elapsed).coerceAtLeast(0).toInt()
}