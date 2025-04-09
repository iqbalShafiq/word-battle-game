package id.usecase.word_battle.network

import android.util.Log
import id.usecase.word_battle.auth.TokenManager
import id.usecase.word_battle.protocol.GameCommand
import id.usecase.word_battle.protocol.GameEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * WebSocket client for game communication
 */
class GameWebSocketClient(
    private val baseUrl: String = "ws://api.wordbattle.usecase.id/game",
    private val tokenManager: TokenManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private val client = HttpClient { install(WebSockets) }

    private val _incomingCommand = MutableSharedFlow<GameEvent>(
        replay = 0,
        extraBufferCapacity = 100
    )
    val incomingCommand: SharedFlow<GameEvent> = _incomingCommand.asSharedFlow()

    private val _connectionStatus = MutableSharedFlow<ConnectionStatus>(
        replay = 1,
        extraBufferCapacity = 0
    )
    val connectionStatus: SharedFlow<ConnectionStatus> = _connectionStatus.asSharedFlow()

    /**
     * Connect to game server WebSocket
     */
    suspend fun connect() {
        try {
            _connectionStatus.emit(ConnectionStatus.CONNECTING)

            val token = tokenManager.getAccessToken()
            if (token.isNullOrBlank()) {
                _connectionStatus.emit(ConnectionStatus.FAILED)
                return
            }

            client.webSocket(
                method = HttpMethod.Get,
                host = baseUrl.removePrefix("ws://").removePrefix("wss://").substringBefore("/"),
                path = baseUrl.substringAfter("/", ""),
                request = {
                    headers.append("Authorization", "Bearer $token")
                }
            ) {
                webSocketSession = this
                _connectionStatus.emit(ConnectionStatus.CONNECTED)

                // Start receiving messages
                scope.launch {
                    try {
                        while (isActive) {
                            val frame = incoming.receive()
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()
                                    try {
                                        val message = Json.decodeFromString<GameEvent>(text)
                                        _incomingCommand.emit(message)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error parsing message: $text", e)
                                    }
                                }

                                else -> { /* Ignore other frame types */
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in WebSocket receive loop", e)
                        _connectionStatus.emit(ConnectionStatus.DISCONNECTED)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket connection failed", e)
            _connectionStatus.emit(ConnectionStatus.FAILED)
        }
    }

    /**
     * Send message to WebSocket server
     */
    suspend fun sendCommand(message: GameCommand) {
        try {
            val session = webSocketSession ?: run {
                Log.e(TAG, "Attempted to send message with no active session")
                return
            }

            session.send(Frame.Text(Json.encodeToString(message)))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            reconnect()
        }
    }

    /**
     * Disconnect from WebSocket server
     */
    suspend fun disconnect() {
        try {
            webSocketSession?.close()
            webSocketSession = null
            _connectionStatus.emit(ConnectionStatus.DISCONNECTED)
        } catch (e: Exception) {
            Log.e(TAG, "Error closing WebSocket", e)
        }
    }

    /**
     * Attempt to reconnect to WebSocket server
     */
    suspend fun reconnect() {
        disconnect()
        connect()
    }

    /**
     * Release resources
     */
    fun release() {
        scope.cancel()
        scope.launch {
            disconnect()
        }
    }

    companion object {
        private val TAG = GameWebSocketClient::class.java.simpleName
    }
}