package id.usecase.word_battle.models

import id.usecase.word_battle.protocol.GameEvent
import kotlinx.serialization.Serializable

@Serializable
data class WebSocketEventMessage(
    val type: String,
    val event: GameEvent
)