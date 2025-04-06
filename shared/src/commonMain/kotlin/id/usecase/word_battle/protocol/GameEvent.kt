package id.usecase.word_battle.protocol

import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.models.GameRound
import kotlinx.serialization.Serializable

/**
 * Events sent from server to clients
 */
@Serializable
sealed class GameEvent {
    /**
     * Player joined the queue
     */
    @Serializable
    data class QueueJoined(
        val playerId: String,
        val position: Int,
        val estimatedWaitTime: Int
    ) : GameEvent()

    /**
     * Game match found and created
     */
    @Serializable
    data class GameCreated(
        val gameId: String,
        val players: List<GamePlayer>,
        val gameMode: GameMode
    ) : GameEvent()

    /**
     * New round started
     */
    @Serializable
    data class RoundStarted(
        val gameId: String,
        val round: GameRound,
        val timeLimit: Int
    ) : GameEvent()

    /**
     * Word submission result
     */
    @Serializable
    data class WordResult(
        val playerId: String,
        val gameId: String,
        val word: String,
        val isValid: Boolean,
        val score: Int
    ) : GameEvent()

    /**
     * Round ended with results
     */
    @Serializable
    data class RoundEnded(
        val gameId: String,
        val roundId: String,
        val results: Map<String, Int>, // player ID to score
        val winningWord: String,
        val winningPlayerId: String?
    ) : GameEvent()

    /**
     * Game ended with final results
     */
    @Serializable
    data class GameEnded(
        val gameId: String,
        val results: Map<String, Int>, // player ID to total score
        val winnerId: String?,
        val reason: String = "Game completed"
    ) : GameEvent()

    /**
     * Chat message from another player
     */
    @Serializable
    data class ChatReceived(
        val playerId: String,
        val username: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : GameEvent()

    /**
     * Error notification
     */
    @Serializable
    data class Error(
        val message: String,
        val code: Int = 0
    ) : GameEvent()
}