package id.usecase.word_battle.protocol

import kotlinx.serialization.Serializable

@Serializable
enum class ErrorCode {
    INVALID_WORD,
    NOT_YOUR_TURN,
    ROOM_FULL,
    GAME_ALREADY_STARTED,
    INTERNAL_ERROR,
    INVALID_MESSAGE
}