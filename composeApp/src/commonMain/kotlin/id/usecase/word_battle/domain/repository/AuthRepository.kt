package id.usecase.word_battle.domain.repository

import id.usecase.word_battle.models.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository for authentication
 */
interface AuthRepository {
    /**
     * Login with username and password
     */
    suspend fun login(username: String, password: String): Result<UserProfile>

    /**
     * Register new user
     */
    suspend fun register(username: String, password: String): Result<UserProfile>

    /**
     * Get current logged-in user
     */
    suspend fun getCurrentUser(): UserProfile?

    /**
     * Logout current user
     */
    suspend fun logout()

    /**
     * Observe authentication state changes
     */
    fun observeAuthState(): Flow<Boolean>
}