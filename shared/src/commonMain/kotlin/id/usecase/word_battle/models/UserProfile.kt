package id.usecase.word_battle.models

import kotlinx.serialization.Serializable

/**
 * Simplified user profile for sharing between platforms
 */
@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val stats: UserStats = UserStats()
)