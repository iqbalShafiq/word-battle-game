package id.usecase.word_battle.network

import id.usecase.word_battle.network.auth.AuthRequest
import id.usecase.word_battle.network.auth.AuthResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/**
 * API service for authentication endpoints
 */
class AuthApi(
    private val httpClient: HttpClient
) {
    /**
     * Login with username and password
     */
    suspend fun login(username: String, password: String): AuthResponse {
        return httpClient.post("/auth/login") {
            setBody(AuthRequest(username = username))
        }.body()
    }

    /**
     * Register new user
     */
    suspend fun register(username: String, password: String): AuthResponse {
        return httpClient.post("/auth/register") {
            setBody(AuthRequest(username = username))
        }.body()
    }
}