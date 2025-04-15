package id.usecase.word_battle.protocol

import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.models.GameRound
import kotlinx.serialization.SerialName
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
    @SerialName("id.usecase.word_battle.protocol.GameEvent.QueueJoined")
    data class QueueJoined(
        val playerId: String,
        val position: Int,
        val estimatedWaitTime: Int
    ) : GameEvent()

    /**
     * Game match found and created
     */
    @Serializable
    @SerialName("id.usecase.word_battle.protocol.GameEvent.GameCreated")
    data class GameCreated(
        val gameId: String,
        val players: List<GamePlayer>,
        val gameMode: GameMode
    ) : GameEvent()

    /**
     * New round started
     */
    @Serializable
    @SerialName("id.usecase.word_battle.protocol.GameEvent.RoundStarted")
    data class RoundStarted(
        val gameId: String,
        val round: GameRound,
        val timeLimit: Int
    ) : GameEvent()

    /**
     * Word submission result
     */
    @Serializable
    @SerialName("id.usecase.word_battle.protocol.GameEvent.WordResult")
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
    @SerialName("id.usecase.word_battle.protocol.GameEvent.RoundEnded")
    data class RoundEnded(
        val gameId: String,
        val roundId: String,
        val results: Map<String, Int>,
        val winningWord: String,
        val winningPlayerId: String?
    ) : GameEvent()

    /**
     * Game ended with final results
     */
    @Serializable
    @SerialName("id.usecase.word_battle.protocol.GameEvent.GameEnded")
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
    @SerialName("id.usecase.word_battle.protocol.GameEvent.ChatReceived")
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
    @SerialName("id.usecase.word_battle.protocol.GameEvent.Error")
    data class Error(
        val message: String,
        val code: Int = 0
    ) : GameEvent()
}