package id.usecase.word_battle.network

import id.usecase.word_battle.auth.TokenManager
import id.usecase.word_battle.models.UserProfile
import id.usecase.word_battle.models.UserStats
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody

/**
 * API service for user operations
 */
class UserApi(
    private val httpClient: HttpClient,
    private val tokenManager: TokenManager
) {
    /**
     * Get current user profile
     */
    suspend fun getCurrentUser(): UserProfile {
        return httpClient.get("/auth/profile") {
            tokenManager.addAuthHeader(this)
        }.body()
    }

    /**
     * Get user stats
     */
    suspend fun getUserStats(): UserStats {
        return httpClient.get("/user/stats") {
            tokenManager.addAuthHeader(this)
        }.body()
    }

    /**
     * Update username
     */
    suspend fun updateUsername(newUsername: String): UserProfile {
        return httpClient.put("/user/username") {
            tokenManager.addAuthHeader(this)
            setBody(mapOf("username" to newUsername))
        }.body()
    }
}