package id.usecase.word_battle.data.models.game

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * Represents a game session
 */
@Serializable
data class GameSession(
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = Instant.now().epochSecond,
    val endedAt: Long? = null,
    val players: List<String> = emptyList(),
    val winnerId: String? = null,
    val gameMode: GameMode = GameMode.CLASSIC,
    val isActive: Boolean = true
)
