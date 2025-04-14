package id.usecase.word_battle.models

import id.usecase.word_battle.protocol.GameCommand
import id.usecase.word_battle.protocol.GameEvent
import kotlinx.serialization.Serializable

@Serializable
data class WebSocketCommandMessage(
    val type: String,
    val command: GameCommand
)