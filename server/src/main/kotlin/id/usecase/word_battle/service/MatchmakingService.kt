package id.usecase.word_battle.service

import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.protocol.GameEvent
import id.usecase.word_battle.websocket.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Service that handles matchmaking between players
 */
class MatchmakingService(
    private val gameService: GameService,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val scope = CoroutineScope(Dispatchers.Default)

    // Mutex to safely modify queues during matchmaking
    private val mutex = Mutex()

    // Queue by game mode
    private val queues = ConcurrentHashMap<GameMode, CopyOnWriteArrayList<String>>()

    // Required players for different game modes
    private val requiredPlayers = mapOf(
        GameMode.CLASSIC to 2,
        GameMode.VOICE_BATTLE to 2,
        GameMode.ASYMMETRIC to 2,
        GameMode.TIME_ATTACK to 2
    )

    init {
        // Initialize queues for each game mode
        GameMode.values().forEach { mode ->
            queues[mode] = CopyOnWriteArrayList()
        }

        // Start periodic matchmaking check
        startPeriodicMatchmaking()
    }

    /**
     * Add a player to the matchmaking queue for a specific game mode
     * @return Position in queue
     */
    suspend fun addPlayerToQueue(playerId: String, gameMode: GameMode): Int {
        return mutex.withLock {
            // Get queue for game mode
            val queue = queues[gameMode] ?: run {
                queues[gameMode] = CopyOnWriteArrayList()
                queues[gameMode]!!
            }

            // Remove if already in queue (avoid duplicates)
            queue.remove(playerId)

            // Add to queue
            queue.add(playerId)

            // Return position (1-based index for user-friendliness)
            queue.indexOf(playerId) + 1
        }
    }

    /**
     * Remove a player from all queues
     * @return True if the player was removed from any queue
     */
    suspend fun removePlayerFromQueue(playerId: String): Boolean {
        return mutex.withLock {
            var removed = false

            queues.values.forEach { queue ->
                if (queue.remove(playerId)) {
                    removed = true
                }
            }

            removed
        }
    }

    /**
     * Process matchmaking for a specific game mode
     */
    suspend fun processMatchmaking(gameMode: GameMode) {
        val required = requiredPlayers[gameMode] ?: return

        mutex.withLock {
            val queue = queues[gameMode] ?: return

            // Check if we have enough players
            if (queue.size >= required) {
                // Get the required number of players from the front of the queue
                val matchedPlayers = queue.take(required)

                // Remove matched players from the queue
                matchedPlayers.forEach { queue.remove(it) }

                // Create a game session for these players
                createGameSession(matchedPlayers, gameMode)
            }
        }
    }

    /**
     * Create a game session for the matched players
     */
    private suspend fun createGameSession(playerIds: List<String>, gameMode: GameMode) {
        try {
            // Create the game session
            val gameSession = gameService.createGameSession(playerIds, gameMode)

            if (gameSession != null) {
                logger.info("Created game session ${gameSession.id} for players $playerIds")

                // Convert player IDs to GamePlayer objects
                val gamePlayers = playerIds.mapNotNull { playerId ->
                    val profile = userService.getPlayerProfile(playerId)
                    profile?.let {
                        id.usecase.word_battle.models.GamePlayer(
                            id = profile.id,
                            username = profile.username
                        )
                    }
                }

                // Notify all players that a game has been created
                val gameCreatedEvent = GameEvent.GameCreated(
                    gameId = gameSession.id,
                    players = gamePlayers,
                    gameMode = gameMode
                )

                SessionManager.sendEventToPlayers(playerIds, gameCreatedEvent)

                // Start the first round automatically after a short delay
                startGameAfterDelay(gameSession.id)
            } else {
                logger.error("Failed to create game session for players $playerIds")

                // Return players to the front of the queue
                playerIds.forEach { playerId ->
                    addPlayerToQueue(playerId, gameMode)
                }
            }
        } catch (e: Exception) {
            logger.error("Error creating game session: ${e.message}")
        }
    }

    /**
     * Start the game after a short delay
     */
    private fun startGameAfterDelay(gameId: String) {
        scope.launch {
            // Wait a moment to let players see who they're matched with
            delay(5000)

            // Start the game (first round)
            gameService.startGame(gameId)
        }
    }

    /**
     * Start periodic matchmaking checks
     */
    private fun startPeriodicMatchmaking() {
        scope.launch {
            while (true) {
                try {
                    // Process matchmaking for each game mode
                    GameMode.entries.forEach { gameMode ->
                        processMatchmaking(gameMode)
                    }
                } catch (e: Exception) {
                    logger.error("Error in periodic matchmaking: ${e.message}")
                }

                // Wait before next check
                delay(5000)
            }
        }
    }

    /**
     * Get the current queue size for a game mode
     */
    fun getQueueSize(gameMode: GameMode): Int {
        return queues[gameMode]?.size ?: 0
    }
}