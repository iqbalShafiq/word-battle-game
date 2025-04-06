package id.usecase.word_battle.game

import id.usecase.word_battle.data.models.game.GameSession
import id.usecase.word_battle.data.repository.GameRepository
import id.usecase.word_battle.protocol.GameEvent
import id.usecase.word_battle.service.GameService
import id.usecase.word_battle.websocket.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Manages active game rooms and their state
 */
class GameRoomManager : KoinComponent {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val gameService: GameService by inject()
    private val gameRepository: GameRepository by inject()

    // Active game rooms with their state
    private val activeRooms = ConcurrentHashMap<String, GameRoom>()

    // Dispatcher for scheduling game tasks
    private val gameDispatcher = Executors.newScheduledThreadPool(2).asCoroutineDispatcher()
    private val gameScope = CoroutineScope(gameDispatcher + SupervisorJob())

    /**
     * Create a new game room
     */
    suspend fun createRoom(gameSession: GameSession): GameRoom {
        val room = GameRoom(
            gameId = gameSession.id,
            playerIds = gameSession.players.toMutableList(),
            gameMode = gameSession.gameMode,
            createdAt = System.currentTimeMillis(),
            scope = gameScope,
            gameService = gameService
        )

        // Store in active rooms
        activeRooms[gameSession.id] = room
        logger.info("Created game room for game ${gameSession.id} with ${gameSession.players.size} players")

        return room
    }

    /**
     * Get a game room by ID
     */
    fun getRoom(gameId: String): GameRoom? = activeRooms[gameId]

    /**
     * Start a game in a room
     */
    suspend fun startGame(gameId: String) {
        val room = activeRooms[gameId] ?: return
        room.start()
    }

    /**
     * End and cleanup a game room
     */
    suspend fun endRoom(gameId: String) {
        val room = activeRooms.remove(gameId) ?: return
        room.cleanup()
        logger.info("Game room for game $gameId has been ended and cleaned up")
    }

    /**
     * Handle player disconnection
     */
    suspend fun handlePlayerDisconnect(playerId: String) {
        // Find rooms where this player was participating
        val roomsWithPlayer = activeRooms.values.filter { playerId in it.playerIds }

        for (room in roomsWithPlayer) {
            room.playerDisconnected(playerId)

            // If room is now empty or can't continue, clean it up
            if (room.playerIds.isEmpty() || !room.canContinue()) {
                endRoom(room.gameId)
            }
        }
    }

    /**
     * Get active room count
     */
    fun getActiveRoomCount(): Int = activeRooms.size

    /**
     * Regular cleanup of stale game rooms
     */
    fun startCleanupTask() {
        gameScope.launch {
            while (isActive) {
                try {
                    cleanupStaleRooms()
                } catch (e: Exception) {
                    logger.error("Error during room cleanup: ${e.message}")
                }
                delay(5 * 60 * 1000) // Run every 5 minutes
            }
        }
    }

    /**
     * Remove stale or inactive game rooms
     */
    private suspend fun cleanupStaleRooms() {
        val now = System.currentTimeMillis()
        val staleThreshold = 2 * 60 * 60 * 1000 // 2 hours

        val staleRooms = activeRooms.values.filter { (now - it.lastActivity) > staleThreshold }

        for (room in staleRooms) {
            logger.info("Cleaning up stale room for game ${room.gameId}")
            endRoom(room.gameId)

            // Notify any remaining connected players
            SessionManager.sendEventToPlayers(
                room.playerIds,
                GameEvent.GameEnded(
                    gameId = room.gameId,
                    results = emptyMap(),
                    winnerId = null,
                    reason = "Game timed out due to inactivity"
                )
            )
        }

        logger.info("Cleanup complete. Removed ${staleRooms.size} stale rooms")
    }

    /**
     * Shutdown the manager and all active rooms
     */
    suspend fun shutdown() {
        gameScope.cancel()

        // Clean up all rooms
        for (gameId in activeRooms.keys.toList()) {
            endRoom(gameId)
        }

        // Shutdown dispatcher
        gameDispatcher.close()
        logger.info("GameRoomManager has been shut down")
    }
}