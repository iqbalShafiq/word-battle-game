package id.usecase.word_battle.websocket

import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.protocol.GameEvent
import id.usecase.word_battle.protocol.MessageType
import id.usecase.word_battle.protocol.WebSocketMessage
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * Manages a player's WebSocket connection
 */
class PlayerSession(
    val player: GamePlayer,
    val socket: DefaultWebSocketSession
) {
    private val logger = LoggerFactory.getLogger(PlayerSession::class.java)

    /**
     * Send a game event to this player
     */
    suspend fun sendEvent(event: GameEvent) {
        try {
            val message = WebSocketMessage(
                type = MessageType.EVENT,
                event = event
            )
            socket.send(Frame.Text(Json.encodeToString(message)))
        } catch (_: ClosedSendChannelException) {
            logger.warn("Failed to send event to player ${player.id}: channel closed")
        } catch (e: Exception) {
            logger.error("Error sending event to player ${player.id}: ${e.message}")
        }
    }

    /**
     * Send a ping to keep the connection alive
     */
    suspend fun sendPing() {
        try {
            val pingMessage = WebSocketMessage(type = MessageType.PING)
            socket.send(Frame.Text(Json.encodeToString(pingMessage)))
        } catch (e: Exception) {
            logger.warn("Failed to send ping to player ${player.id}: ${e.message}")
        }
    }
}