package id.usecase.word_battle.data.models.game

import id.usecase.word_battle.models.WordSubmission
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Represents a word submission by a player
 */
@Serializable
data class WordSubmissionEntity(
    val playerId: String,
    val word: String,
    val timestamp: Long = Instant.now().epochSecond,
    val isValid: Boolean = false,
    val score: Int = 0
)

/**
 * Extension function to convert submission entity to shared model
 */
fun WordSubmissionEntity.toSharedModel(): WordSubmission {
    return WordSubmission(
        playerId = this.playerId,
        word = this.word,
        timestamp = this.timestamp,
        isValid = this.isValid,
        score = this.score
    )
}
