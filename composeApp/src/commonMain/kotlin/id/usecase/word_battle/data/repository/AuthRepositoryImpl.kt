package id.usecase.word_battle.data.repository

import id.usecase.word_battle.domain.model.User
import id.usecase.word_battle.domain.model.UserStats
import id.usecase.word_battle.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of AuthRepository
 */
class AuthRepositoryImpl : AuthRepository {

    private val authStateFlow = MutableStateFlow(false)
    private var currentUser: User? = null

    override suspend fun login(username: String, password: String): Result<User> {
        // In a real app, this would make an API call
        // For now, we'll simulate successful login
        return try {
            // Create dummy user
            val user = User(
                id = "user-${System.currentTimeMillis()}",
                username = username,
                stats = UserStats()
            )

            // Store logged in user
            currentUser = user
            authStateFlow.value = true

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(username: String, password: String): Result<User> {
        // Similar to login, but would create a new user account
        return login(username, password)
    }

    override suspend fun getCurrentUser(): User? {
        return currentUser
    }

    override suspend fun logout() {
        currentUser = null
        authStateFlow.value = false
    }

    override fun observeAuthState(): Flow<Boolean> {
        return authStateFlow
    }
}