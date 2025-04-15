package id.usecase.word_battle.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TIME_OUT = 60_000L

/**
 * Ktor client setup for API calls
 */
object KtorClient {

    private const val TAG = "KtorClient"

    // Base URL for API endpoints - update with your actual API URL
    private const val BASE_URL = "http://192.168.11.41:8080/"

    // Create and configure Ktor HttpClient
    fun create(enableNetworkLogs: Boolean = true): HttpClient {
        return HttpClient(CIO) {
            // Set up timeout
            install(HttpTimeout) {
                requestTimeoutMillis = TIME_OUT
                connectTimeoutMillis = TIME_OUT
                socketTimeoutMillis = TIME_OUT
            }

            // Set up content negotiation with JSON
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }

            // Optional logging
            if (enableNetworkLogs) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d(TAG, message)
                        }
                    }
                    level = LogLevel.ALL
                }

                install(ResponseObserver) {
                    onResponse { response ->
                        Log.d(TAG, "HTTP status: ${response.status.value}")
                    }
                }
            }

            // Default request configuration
            defaultRequest {
                url(BASE_URL)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }
}