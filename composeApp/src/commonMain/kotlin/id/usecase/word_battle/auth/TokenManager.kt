package id.usecase.word_battle.auth

import android.content.SharedPreferences
import androidx.core.content.edit
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages authentication tokens for the app
 */
class TokenManager(private val prefs: SharedPreferences) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: Flow<AuthState> = _authState.asStateFlow()
    private val isRefreshing = AtomicBoolean(false)

    init {
        // Check if we have a saved token on init
        val accessToken = getAccessToken()
        if (!accessToken.isNullOrBlank()) {
            _authState.value = AuthState.Authenticated
        }
    }

    /**
     * Save tokens to secure storage
     */
    fun saveTokens(accessToken: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
        }

        _authState.value = AuthState.Authenticated
    }

    /**
     * Get the current access token
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Clear all tokens (logout)
     */
    fun clearTokens() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
        }

        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Start a token refresh operation if not already in progress
     */
    fun startTokenRefresh(): Boolean {
        return isRefreshing.compareAndSet(false, true)
    }

    /**
     * Complete a token refresh operation
     */
    fun completeTokenRefresh() {
        isRefreshing.set(false)
    }

    /**
     * Auth interceptor function to add auth header to requests
     */
    fun addAuthHeader(builder: io.ktor.client.request.HttpRequestBuilder) {
        getAccessToken()?.let { token ->
            builder.header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}