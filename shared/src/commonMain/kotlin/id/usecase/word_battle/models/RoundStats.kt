package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

@Serializable
data class RoundStats(
    val roundNumber: Int,
    val letters: String,
    val submissions: List<WordSubmission>,
    val validWords: Set<String>
)