package id.usecase.word_battle.network.game

import kotlinx.serialization.Serializable

@Serializable
data class LeaveMatchmakingRequest(
    val queueId: String
)