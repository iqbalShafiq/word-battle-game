package id.usecase.word_battle.network.auth

import id.usecase.word_battle.models.UserProfile
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: UserProfile?
)