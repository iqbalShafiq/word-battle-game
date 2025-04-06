package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

/**
 * Available game modes
 */
@Serializable
enum class GameMode {
    CLASSIC,
    VOICE_BATTLE,
    ASYMMETRIC,
    TIME_ATTACK
}