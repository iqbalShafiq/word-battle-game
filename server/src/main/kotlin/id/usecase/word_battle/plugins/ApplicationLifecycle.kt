package id.usecase.word_battle.plugins

import id.usecase.word_battle.game.GameRoomManager
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.ApplicationStopping
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.ktor.ext.getKoin
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

/**
 * Manages application lifecycle and scheduled tasks
 */
class ApplicationLifecycle(
    private val gameRoomManager: GameRoomManager
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Start application services and scheduled tasks
     */
    fun start() {
        logger.info("Starting application lifecycle...")

        // Start room cleanup job
        gameRoomManager.startCleanupTask()

        // Start periodic status logging
        startPeriodicStatusLogging()

        logger.info("Application lifecycle started")
    }

    /**
     * Shutdown application gracefully
     */
    suspend fun shutdown() {
        logger.info("Shutting down application...")

        // Cancel all running jobs
        scope.cancel()

        // Shutdown game room manager
        withTimeoutOrNull(10000) {
            gameRoomManager.shutdown()
        }

        logger.info("Application shutdown completed")
    }

    /**
     * Log periodic status information
     */
    private fun startPeriodicStatusLogging() {
        scope.launch {
            while (isActive) {
                try {
                    val activeRooms = gameRoomManager.getActiveRoomCount()

                    logger.info("Status: Active game rooms: $activeRooms")
                } catch (e: Exception) {
                    logger.error("Error in status logging: ${e.message}")
                }

                delay(5.minutes)
            }
        }
    }
}

/**
 * Configure application lifecycle
 */
fun Application.configureLifecycle() {
    // Get GameRoomManager from Koin
    val gameRoomManager = getKoin().get<GameRoomManager>()

    // Create lifecycle manager with explicit dependency
    val lifecycle = ApplicationLifecycle(gameRoomManager)

    // Start services
    lifecycle.start()

    // Register shutdown hook
    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            lifecycle.shutdown()
        }
    }
}