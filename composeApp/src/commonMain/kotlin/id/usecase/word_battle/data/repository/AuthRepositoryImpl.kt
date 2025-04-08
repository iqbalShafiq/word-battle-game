package id.usecase.word_battle.data.repository

import android.util.Log
import id.usecase.word_battle.auth.AuthState
import id.usecase.word_battle.auth.TokenManager
import id.usecase.word_battle.domain.repository.AuthRepository
import id.usecase.word_battle.models.UserProfile
import id.usecase.word_battle.network.AuthApi
import id.usecase.word_battle.network.UserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of AuthRepository
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<UserProfile> {
        return try {
            // Call login API
            val authResponse = authApi.login(username, password)

            // Save tokens
            tokenManager.saveTokens(
                accessToken = authResponse.token.toString()
            )

            // Get user details with the new token
            val user = userApi.getCurrentUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(username: String, password: String): Result<UserProfile> {
        return try {
            // Call register API
            val authResponse = authApi.register(username, password)

            // Save tokens
            tokenManager.saveTokens(
                accessToken = authResponse.token.toString()
            )

            // Get user details with the new token
            val user = userApi.getCurrentUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): UserProfile? {
        return try {
            userApi.getCurrentUser()
        } catch (e: Exception) {
            Log.d(TAG, "getCurrentUser: Exception occurred when : ${e.message}")
            null
        }
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }

    override fun observeAuthState(): Flow<Boolean> {
        return tokenManager.authState.map { it is AuthState.Authenticated }
    }

    companion object {
        private val TAG = AuthRepositoryImpl::class.java.simpleName
    }
}