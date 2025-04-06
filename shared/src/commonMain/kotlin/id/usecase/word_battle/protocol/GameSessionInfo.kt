package id.usecase.word_battle.protocol

import id.usecase.word_battle.models.GamePlayer
import kotlinx.serialization.Serializable

/**
 * Represents active game session information
 */
@Serializable
data class GameSessionInfo(
    val gameId: String,
    val players: List<GamePlayer>,
    val currentRoundId: String?,
    val currentRoundNumber: Int,
    val gameStatus: GameStatus,
    val startTime: Long,
    val timePerRound: Int = 60, // seconds
    val maxRounds: Int = 5
)

/**
 * Game session status
 */
@Serializable
enum class GameStatus {
    WAITING_FOR_PLAYERS,
    READY_TO_START,
    IN_PROGRESS,
    ROUND_ENDING,
    COMPLETED,
    CANCELED
}

/**
 * Wrapper for all WebSocket messages
 */
@Serializable
data class WebSocketMessage(
    val type: MessageType,
    val command: GameCommand? = null,
    val event: GameEvent? = null
)

@Serializable
enum class MessageType {
    COMMAND,
    EVENT,
    PING,
    PONG
}