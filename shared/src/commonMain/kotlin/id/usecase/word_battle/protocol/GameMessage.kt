package id.usecase.word_battle.protocol

import id.usecase.word_battle.models.GameResult
import id.usecase.word_battle.models.GameRoom
import id.usecase.word_battle.models.RoundStats
import kotlinx.serialization.Serializable

@Serializable
sealed class GameMessage {
    // Client -> Server messages
    @Serializable
    data class JoinRequest(val username: String) : GameMessage()

    @Serializable
    data class SubmitWord(val word: String) : GameMessage()

    @Serializable
    data class PlayerReady(val ready: Boolean = true) : GameMessage()

    @Serializable
    object LeaveGame : GameMessage()

    // Server -> Client messages
    @Serializable
    data class RoomUpdate(val room: GameRoom) : GameMessage()

    @Serializable
    data class RoundStart(
        val letters: String,
        val timeSeconds: Int
    ) : GameMessage()

    @Serializable
    data class WordResult(
        val word: String,
        val playerId: String,
        val isValid: Boolean,
        val score: Int
    ) : GameMessage()

    @Serializable
    data class RoundResult(
        val roundStats: RoundStats
    ) : GameMessage()

    @Serializable
    data class GameOver(val result: GameResult) : GameMessage()

    @Serializable
    data class Error(val code: ErrorCode, val message: String) : GameMessage()
}