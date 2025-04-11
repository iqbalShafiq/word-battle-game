package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

@Serializable
data class Lobby(
    private val estimatedTime: Int,
    private val state: LobbyState = LobbyState.WAITING,
)

@Serializable
enum class LobbyState {
    WAITING,
    HAS_FOUND_OPPONENT,
}
