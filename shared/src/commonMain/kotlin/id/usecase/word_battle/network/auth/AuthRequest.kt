package id.usecase.word_battle.network.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val username: String
)