package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

@Serializable
data class GamePlayer(
    val id: String,
    val username: String,
    val score: Int = 0,
    val isReady: Boolean = false,
    val isActive: Boolean = true
)
