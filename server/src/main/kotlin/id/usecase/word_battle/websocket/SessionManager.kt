package id.usecase.word_battle.websocket

import id.usecase.word_battle.protocol.GameEvent
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


/**
 * Manages all active WebSocket connections
 */
object SessionManager {
    private val logger = LoggerFactory.getLogger(SessionManager::class.java)

    private val sessions = ConcurrentHashMap<String, PlayerSession>()
    private val activeCount = AtomicInteger(0)

    /**
     * Register a new player session
     */
    fun registerSession(playerId: String, session: PlayerSession) {
        sessions[playerId] = session
        activeCount.incrementAndGet()
        logger.info("Player ${session.player.username} (${playerId}) connected. Active sessions: ${activeCount.get()}")
    }

    /**
     * Remove a player session
     */
    fun removeSession(playerId: String) {
        if (sessions.remove(playerId) != null) {
            activeCount.decrementAndGet()
            logger.info("Player $playerId disconnected. Active sessions: ${activeCount.get()}")
        }
    }

    /**
     * Get a session by player ID
     */
    private fun getSession(playerId: String): PlayerSession? = sessions[playerId]

    /**
     * Send an event to a specific player
     */
    suspend fun sendEventToPlayer(playerId: String, event: GameEvent) {
        getSession(playerId)?.sendEvent(event)
    }

    /**
     * Send an event to multiple players
     */
    suspend fun sendEventToPlayers(playerIds: List<String>, event: GameEvent) {
        playerIds.forEach { playerId ->
            sendEventToPlayer(playerId, event)
        }
    }

    /**
     * Get the number of active connections
     */
    fun getActiveSessionCount(): Int = activeCount.get()
}