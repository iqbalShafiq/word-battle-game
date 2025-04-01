package id.usecase.word_battle.data.models.game

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Represents a word submission by a player
 */
@Serializable
data class WordSubmission(
    val playerId: String,
    val word: String,
    val timestamp: Long = Instant.now().epochSecond,
    val isValid: Boolean = false,
    val score: Int = 0
)
