package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

@Serializable
data class UserStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalScore: Int = 0
)