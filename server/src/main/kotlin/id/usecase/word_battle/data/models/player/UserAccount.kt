package id.usecase.word_battle.data.models.player

import id.usecase.word_battle.models.GamePlayer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * User account model for authentication and persistence
 */
@Serializable
data class UserAccount(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val createdAt: Long = Instant.now().epochSecond,
    val lastActive: Long = Instant.now().epochSecond,
    val stats: PlayerStats = PlayerStats()
)

/**
 * Mapping function to convert UserAccount to GamePlayer
 */
fun UserAccount.toGamePlayer(): GamePlayer = GamePlayer(
    id = this.id,
    username = this.username
)
