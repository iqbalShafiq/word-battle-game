package id.usecase.word_battle.service

import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.data.models.game.GameRoundEntity
import id.usecase.word_battle.data.models.game.GameSession
import id.usecase.word_battle.data.models.game.WordSubmissionEntity
import id.usecase.word_battle.data.models.game.toSharedModel
import id.usecase.word_battle.data.repository.GameRepository
import id.usecase.word_battle.data.repository.WordRepository
import id.usecase.word_battle.network.game.WordSubmissionResponse
import id.usecase.word_battle.protocol.GameEvent
import id.usecase.word_battle.websocket.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * Service for game-related operations
 */
class GameService(
    private val gameRepository: GameRepository,
    private val wordRepository: WordRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val gameScope = CoroutineScope(Dispatchers.Default)

    // Cache for active games and players to help with disconnection handling
    private val activePlayerGames = mutableMapOf<String, MutableSet<String>>() // playerId -> gameIds
    private val gameMaxRounds = 5 // Default max rounds per game
    private val playerReadyStatus = mutableMapOf<String, Boolean>() // playerId -> ready status

    /**
     * Create a new game session
     */
    suspend fun createGameSession(playerIds: List<String>, gameMode: GameMode = GameMode.CLASSIC): GameSession? {
        if (playerIds.size < 2) {
            logger.warn("Cannot create game with less than 2 players")
            return null
        }

        val gameSession = gameRepository.createGameSession(playerIds, gameMode)

        // Track players in this game for disconnect handling
        if (gameSession != null) {
            playerIds.forEach { playerId ->
                activePlayerGames.getOrPut(playerId) { mutableSetOf() }.add(gameSession.id)
            }
        }

        return gameSession
    }

    /**
     * Start a new round in a game
     */
    suspend fun startNewRound(gameSessionId: String, roundNumber: Int): GameRoundEntity? {
        // Generate random letters for the round
        val letters = wordRepository.generateRandomLetters(8)

        return gameRepository.createGameRound(gameSessionId, roundNumber, letters)
    }

    /**
     * Submit a word in a game round
     */
    suspend fun submitWord(roundId: String, playerId: String, word: String): WordSubmissionResponse {
        // Check if word is valid based on dictionary
        val isValid = wordRepository.isValidWord(word)

        // Calculate score (simple algorithm based on word length)
        val score = if (isValid) calculateWordScore(word) else 0

        // Create submission
        val submission = WordSubmissionEntity(
            playerId = playerId,
            word = word,
            isValid = isValid,
            score = score
        )

        // Add submission to database
        val success = gameRepository.addWordSubmission(roundId, submission)

        return WordSubmissionResponse(
            success = success,
            isValid = isValid,
            word = word,
            score = score
        )
    }

    /**
     * Calculate word score based on length and letter complexity
     */
    private fun calculateWordScore(word: String): Int {
        // Simple scoring: 1 point per letter, bonus for longer words
        val baseScore = word.length

        // Bonus for words longer than 5 letters
        val bonus = when {
            word.length >= 8 -> 10
            word.length >= 6 -> 5
            word.length >= 4 -> 2
            else -> 0
        }

        return baseScore + bonus
    }

    /**
     * End a game and determine winner
     */
    suspend fun endGame(gameId: String): GameSession? {
        // Get all rounds for this game
        val rounds = gameRepository.getRoundsForGameSession(gameId)
        if (rounds.isEmpty()) {
            return null
        }

        // Calculate total scores by player
        val playerScores = mutableMapOf<String, Int>()

        rounds.forEach { round ->
            round.submissions.forEach { submission ->
                if (submission.isValid) {
                    val currentScore = playerScores.getOrDefault(submission.playerId, 0)
                    playerScores[submission.playerId] = currentScore + submission.score
                }
            }
        }

        // Find winner (player with highest score)
        val winner = playerScores.entries.maxByOrNull { it.value }?.key

        // End game session
        gameRepository.endGameSession(gameId, winner)

        // Remove from active games tracking
        val gameSession = gameRepository.getGameSession(gameId)
        gameSession?.players?.forEach { playerId ->
            activePlayerGames[playerId]?.remove(gameId)
        }

        return gameSession
    }

    // --- WebSocket-related methods ---

    /**
     * Start a game with WebSocket notifications
     */
    suspend fun startGame(gameId: String) {
        val gameSession = gameRepository.getGameSession(gameId) ?: return

        // Start the first round
        val firstRound = startNewRound(gameId, 1)
        if (firstRound != null) {
            // Convert to shared model for WebSocket
            val sharedRound = firstRound.toSharedModel()

            // Notify all players that the round has started
            SessionManager.sendEventToPlayers(
                gameSession.players,
                GameEvent.RoundStarted(
                    gameId = gameId,
                    round = sharedRound,
                    timeLimit = 60
                )
            )

            // Schedule end of round
            scheduleRoundEnd(gameId, firstRound.id)
        } else {
            logger.error("Failed to start round for game $gameId")
        }
    }

    /**
     * End a round and process results
     */
    suspend fun endRound(gameId: String, roundId: String) {
        // Find the round from the game session
        val gameSession = gameRepository.getGameSession(gameId) ?: return
        val rounds = gameRepository.getRoundsForGameSession(gameId)
        val round = rounds.find { it.id == roundId } ?: return

        // Calculate round scores
        val results = round.submissions
            .filter { it.isValid }
            .groupBy { it.playerId }
            .mapValues { (_, submissions) -> submissions.sumOf { it.score } }

        // Find winning word (highest scoring)
        val highestScoringSubmission = round.submissions
            .filter { it.isValid }
            .maxByOrNull { it.score }

        // Send round ended event
        SessionManager.sendEventToPlayers(
            gameSession.players,
            GameEvent.RoundEnded(
                gameId = gameId,
                roundId = roundId,
                results = results,
                winningWord = highestScoringSubmission?.word ?: "",
                winningPlayerId = highestScoringSubmission?.playerId
            )
        )

        // Check if this was the last round
        if (round.roundNumber >= gameMaxRounds) {
            // End the game
            val finalGame = endGame(gameId)
            if (finalGame != null) {
                // Calculate total scores from all rounds
                val totalScores = calculateFinalScores(gameId)

                // Send game ended event
                SessionManager.sendEventToPlayers(
                    gameSession.players,
                    GameEvent.GameEnded(
                        gameId = gameId,
                        results = totalScores,
                        winnerId = finalGame.winnerId,
                        reason = "Game completed"
                    )
                )
            }
        } else {
            // Start next round after delay
            gameScope.launch {
                delay(5000) // 5 second delay between rounds
                startNextRound(gameId, round.roundNumber + 1)
            }
        }
    }

    /**
     * Start the next round in a game
     */
    private suspend fun startNextRound(gameId: String, roundNumber: Int) {
        val gameSession = gameRepository.getGameSession(gameId) ?: return

        // Start new round
        val nextRound = startNewRound(gameId, roundNumber)
        if (nextRound != null) {
            // Convert to shared model
            val sharedRound = nextRound.toSharedModel()

            // Notify players
            SessionManager.sendEventToPlayers(
                gameSession.players,
                GameEvent.RoundStarted(
                    gameId = gameId,
                    round = sharedRound,
                    timeLimit = 60
                )
            )

            // Schedule end of round
            scheduleRoundEnd(gameId, nextRound.id)
        }
    }

    /**
     * Schedule automatic end of round
     */
    private fun scheduleRoundEnd(gameId: String, roundId: String) {
        gameScope.launch {
            delay(60000) // 60 seconds round time
            endRound(gameId, roundId)
        }
    }

    /**
     * Calculate final scores for a game
     */
    internal suspend fun calculateFinalScores(gameId: String): Map<String, Int> {
        val rounds = gameRepository.getRoundsForGameSession(gameId)

        return rounds.flatMap { round ->
            round.submissions.filter { it.isValid }
        }.groupBy { it.playerId }
            .mapValues { (_, submissions) -> submissions.sumOf { it.score } }
    }

    /**
     * Set player ready status
     */
    suspend fun setPlayerReady(playerId: String): Boolean {
        playerReadyStatus[playerId] = true

        // Check if all players in all active games are ready
        val allPlayersReady = activePlayerGames[playerId]?.all { gameId ->
            val gameSession = gameRepository.getGameSession(gameId)
            gameSession?.players?.all { playerReadyStatus[it] == true } ?: false
        } ?: false

        if (allPlayersReady) {
            activePlayerGames[playerId]?.forEach { gameId ->
                gameScope.launch {
                    startGame(gameId)
                }
            }
        }

        return true
    }

    /**
     * Handle player disconnect
     */
    suspend fun handlePlayerDisconnect(playerId: String) {
        // Get games this player was in
        val gameIds = activePlayerGames[playerId]?.toList() ?: return

        for (gameId in gameIds) {
            val gameSession = gameRepository.getGameSession(gameId) ?: continue

            // Filter out the disconnected player
            val remainingPlayers = gameSession.players.filter { it != playerId }

            if (remainingPlayers.isEmpty()) {
                // End the game if no players left
                endGame(gameId)
                logger.info("Game $gameId ended due to all players disconnected")
            } else {
                // Notify remaining players
                SessionManager.sendEventToPlayers(
                    remainingPlayers,
                    GameEvent.Error("A player has disconnected")
                )
            }
        }

        // Clean up tracking
        activePlayerGames.remove(playerId)
    }

    /**
     * Get all players in a game except the specified player
     */
    suspend fun getOtherPlayersInGame(gameId: String, excludePlayerId: String): List<String> {
        val gameSession = gameRepository.getGameSession(gameId) ?: return emptyList()
        return gameSession.players.filter { it != excludePlayerId }
    }

    /**
     * Get all players in a game
     */
    suspend fun getAllPlayersInGame(gameId: String): List<String> {
        val gameSession = gameRepository.getGameSession(gameId) ?: return emptyList()
        return gameSession.players
    }
}
