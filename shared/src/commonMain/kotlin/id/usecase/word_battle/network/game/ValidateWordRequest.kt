package id.usecase.word_battle.network.game

import kotlinx.serialization.Serializable

@Serializable
data class ValidateWordRequest(
    val word: String,
    val availableLetters: String
)