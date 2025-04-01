package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

@Serializable
enum class GameState {
    WAITING_FOR_PLAYERS,
    STARTING,
    IN_PROGRESS,
    ROUND_ENDING,
    GAME_OVER
}