package id.usecase.word_battle.data.models.player

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * Player model representing a user in the game
 */
@Serializable
data class Player(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val createdAt: Long = Instant.now().epochSecond,
    val lastActive: Long = Instant.now().epochSecond,
    val stats: PlayerStats = PlayerStats()
)
