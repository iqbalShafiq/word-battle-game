package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

@Serializable
data class WordSubmission(
    val playerId: String,
    val word: String,
    val timestamp: Long
)