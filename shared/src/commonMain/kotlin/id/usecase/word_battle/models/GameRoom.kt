package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

@Serializable
data class GameRoom(
    val id: String,
    val gamePlayers: List<GamePlayer>,
    val state: GameState,
    val currentRound: Int = 0,
    val maxRounds: Int = 10,
    val currentLetters: String = ""
)