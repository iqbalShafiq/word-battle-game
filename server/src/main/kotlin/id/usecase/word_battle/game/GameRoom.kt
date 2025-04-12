package id.usecase.word_battle.game

import id.usecase.word_battle.data.models.game.toSharedModel
import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.network.game.WordSubmissionResponse
import id.usecase.word_battle.protocol.GameEvent
import id.usecase.word_battle.protocol.GameStatus
import id.usecase.word_battle.service.GameService
import id.usecase.word_battle.websocket.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.Collections

/**
 * Represents a single game room with active players and game state
 */
class GameRoom(
    val gameId: String,
    val playerIds: MutableList<String>,
    val gameMode: GameMode,
    val createdAt: Long,
    val scope: CoroutineScope,
    private val gameService: GameService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // Game state
    var status: GameStatus = GameStatus.WAITING
        private set

    var currentRoundId: String? = null
        private set

    var currentRoundNumber: Int = 0
        private set

    var lastActivity: Long = createdAt
        private set

    // Player ready status
    private val readyPlayers = Collections.synchronizedSet(mutableSetOf<String>())

    // Timeouts and settings
    private val maxRounds = 5
    private val roundDurationMs = 60_000L // 60 seconds
    private val breakBetweenRoundsMs = 5_000L // 5 seconds

    // Active jobs
    private var currentRoundJob: Job? = null

    /**
     * Start the game
     */
    suspend fun start() {
        if (status != GameStatus.WAITING) {
            logger.warn("Cannot start game $gameId: not in waiting state")
            return
        }

        if (playerIds.size < 2) {
            logger.warn("Cannot start game $gameId: not enough players")
            return
        }

        status = GameStatus.ROUND_ACTIVE
        updateLastActivity()

        // Start first round
        startNextRound()
    }

    /**
     * Player marks themselves as ready
     */
    suspend fun playerReady(playerId: String) {
        if (playerId !in playerIds) return

        readyPlayers.add(playerId)
        updateLastActivity()

        // Check if all players are ready
        if (readyPlayers.size == playerIds.size && status == GameStatus.WAITING) {
            // Start after a short delay
            scope.launch {
                delay(1000)
                start()
            }
        }
    }

    /**
     * Start a new round
     */
    private suspend fun startNextRound() {
        currentRoundNumber++

        if (currentRoundNumber > maxRounds) {
            endGame("Max rounds reached")
            return
        }

        // Create new round
        val round = gameService.startNewRound(gameId, currentRoundNumber)
        if (round == null) {
            logger.error("Failed to create round $currentRoundNumber for game $gameId")
            endGame("Error creating round")
            return
        }

        currentRoundId = round.id
        status = GameStatus.ROUND_ACTIVE
        updateLastActivity()

        // Convert to shared model and notify players
        val sharedRound = round.toSharedModel()
        SessionManager.sendEventToPlayers(
            playerIds,
            GameEvent.RoundStarted(
                gameId = gameId,
                round = sharedRound,
                timeLimit = (roundDurationMs / 1000).toInt()
            )
        )

        // Schedule round end
        currentRoundJob = scope.launch {
            delay(roundDurationMs)
            endRound()
        }
    }

    /**
     * Submit a word during the current round
     */
    suspend fun submitWord(playerId: String, word: String): WordSubmissionResponse {
        if (status != GameStatus.ROUND_ACTIVE || currentRoundId == null) {
            return WordSubmissionResponse(
                success = false,
                isValid = false,
                word = word,
                score = 0
            )
        }

        updateLastActivity()
        return gameService.submitWord(currentRoundId!!, playerId, word)
    }

    /**
     * End the current round
     */
    private suspend fun endRound() {
        if (currentRoundId == null) return

        val roundId = currentRoundId!!

        // Change status
        status = GameStatus.ROUND_OVER
        updateLastActivity()

        // Let the game service handle round end logic
        gameService.endRound(gameId, roundId)

        // Schedule next round after a break
        if (currentRoundNumber < maxRounds) {
            scope.launch {
                delay(breakBetweenRoundsMs)
                startNextRound()
            }
        } else {
            endGame("All rounds completed")
        }
    }

    /**
     * End the game
     */
    private suspend fun endGame(reason: String) {
        status = GameStatus.GAME_OVER
        updateLastActivity()

        // Cancel any active jobs
        currentRoundJob?.cancel()

        // Let the game service handle game end logic
        val finalGame = gameService.endGame(gameId)

        // Calculate final scores
        val playerScores = gameService.calculateFinalScores(gameId)

        if (finalGame != null) {
            // Send game ended event
            SessionManager.sendEventToPlayers(
                playerIds,
                GameEvent.GameEnded(
                    gameId = gameId,
                    results = playerScores,
                    winnerId = finalGame.winnerId,
                    reason = reason
                )
            )
        }
    }

    /**
     * Handle player disconnection
     */
    suspend fun playerDisconnected(playerId: String) {
        playerIds.remove(playerId)
        readyPlayers.remove(playerId)
        updateLastActivity()

        // If not enough players, end the game
        if (playerIds.size < 2 && status != GameStatus.GAME_OVER) {
            endGame("Not enough players")
        }
    }

    /**
     * Check if the game can continue
     */
    fun canContinue(): Boolean = playerIds.size >= 2

    /**
     * Update last activity timestamp
     */
    private fun updateLastActivity() {
        lastActivity = System.currentTimeMillis()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        currentRoundJob?.cancel()
    }
}