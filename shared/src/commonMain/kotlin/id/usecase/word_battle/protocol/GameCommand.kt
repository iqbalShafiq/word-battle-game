package id.usecase.word_battle.protocol

import id.usecase.word_battle.models.GameMode
import kotlinx.serialization.SerialName
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
    @SerialName("JoinQueue")
    data class JoinQueue(
        val playerId: String,
        val gameMode: GameMode = GameMode.CLASSIC
    ) : GameCommand()

    /**
     * Leave the matchmaking queue
     */
    @Serializable
    @SerialName("LeaveQueue")
    data class LeaveQueue(val playerId: String) : GameCommand()

    /**
     * Mark player as ready to start the game
     */
    @Serializable
    @SerialName("Ready")
    data class Ready(val playerId: String) : GameCommand()

    /**
     * Submit a word during a round
     */
    @Serializable
    @SerialName("SubmitWord")
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
    @SerialName("EndRound")
    data class EndRound(
        val playerId: String,
        val gameId: String,
        val roundId: String
    ) : GameCommand()

    /**
     * Send a chat message to other players
     */
    @Serializable
    @SerialName("ChatMessage")
    data class ChatMessage(
        val playerId: String,
        val gameId: String,
        val message: String
    ) : GameCommand()

    /**
     * Disconnect from the game
     */
    @Serializable
    @SerialName("Disconnect")
    data class Disconnect(val playerId: String) : GameCommand()
}