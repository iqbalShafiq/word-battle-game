package id.usecase.word_battle.protocol

import id.usecase.word_battle.models.GameMode
import kotlinx.serialization.Serializable

/**
 * Commands sent from client to server
 */
@Serializable
sealed class GameCommand {
    /**
     * Join the matchmaking queue
     */
    @Serializable
    data class JoinQueue(
        val playerId: String,
        val gameMode: GameMode = GameMode.CLASSIC
    ) : GameCommand()

    /**
     * Leave the matchmaking queue
     */
    @Serializable
    data class LeaveQueue(val playerId: String) : GameCommand()

    /**
     * Mark player as ready to start the game
     */
    @Serializable
    data class Ready(val playerId: String) : GameCommand()

    /**
     * Submit a word during a round
     */
    @Serializable
    data class SubmitWord(
        val playerId: String,
        val gameId: String,
        val roundId: String,
        val word: String
    ) : GameCommand()

    /**
     * Request to end the current round
     */
    @Serializable
    data class EndRound(
        val playerId: String,
        val gameId: String,
        val roundId: String
    ) : GameCommand()

    /**
     * Send a chat message to other players
     */
    @Serializable
    data class ChatMessage(
        val playerId: String,
        val gameId: String,
        val message: String
    ) : GameCommand()

    /**
     * Disconnect from the game
     */
    @Serializable
    data class Disconnect(val playerId: String) : GameCommand()
}