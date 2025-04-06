package id.usecase.word_battle.network.game

import kotlinx.serialization.Serializable

@Serializable
data class WordSubmissionResponse(
    val success: Boolean,
    val isValid: Boolean,
    val word: String,
    val score: Int
)