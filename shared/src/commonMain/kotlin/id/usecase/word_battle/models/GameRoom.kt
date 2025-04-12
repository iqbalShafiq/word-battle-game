package id.usecase.word_battle.models

import id.usecase.word_battle.protocol.GameStatus
import kotlinx.serialization.Serializable

@Serializable
data class GameRoom(
    val id: String,
    val gamePlayers: List<GamePlayer>,
    val state: GameStatus,
    val currentRound: Int = 0,
    val currentRoundId: String = "",
    val maxRounds: Int = 10,
    val currentLetters: String = ""
)