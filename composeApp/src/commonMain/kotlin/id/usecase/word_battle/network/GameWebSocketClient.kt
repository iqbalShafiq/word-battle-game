package id.usecase.word_battle.network

import id.usecase.word_battle.PlatformLogger
import id.usecase.word_battle.auth.TokenManager
import id.usecase.word_battle.models.WebSocketCommandMessage
import id.usecase.word_battle.models.WebSocketEventMessage
import id.usecase.word_battle.protocol.GameCommand
import id.usecase.word_battle.protocol.GameEvent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

/**
 * WebSocket client for game communication
 */
class GameWebSocketClient(
    private val baseUrl: String = "ws://192.168.11.41:8080/game",
    private val tokenManager: TokenManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 20.seconds
        }
    }

    private val _incomingEvent = MutableSharedFlow<GameEvent>(
        replay = 0,
        extraBufferCapacity = 100
    )
    val incomingEvent: SharedFlow<GameEvent> = _incomingEvent.asSharedFlow()

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
                PlatformLogger.debug(TAG, "No token available")
                _connectionStatus.emit(ConnectionStatus.FAILED)
                return
            }

            val wsUrl = baseUrl.removePrefix("ws://")
            val hostAndPort = wsUrl.substringBefore("/")
            val host = hostAndPort.substringBefore(":")
            val port = hostAndPort.substringAfter(":").toInt()
            val path = wsUrl.substringAfter(hostAndPort) + "?token=$token"

            PlatformLogger.debug(TAG, "Connecting to WebSocket: $baseUrl")

            client.webSocket(
                method = HttpMethod.Get,
                host = host,
                port = port,
                path = path
            ) {
                webSocketSession = this
                _connectionStatus.emit(ConnectionStatus.CONNECTED)
                PlatformLogger.debug(TAG, "WebSocket connected")

                try {
                    // Start receiving messages in the same coroutine
                    while (isActive) {
                        val frame = incoming.receive()
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                try {
                                    val json = Json {
                                        ignoreUnknownKeys = true
                                        isLenient = true
                                    }
                                    val message = json.decodeFromString<WebSocketEventMessage>(text)
                                    PlatformLogger.debug(TAG, "Received message: $text")
                                    _incomingEvent.emit(message.event)
                                } catch (e: Exception) {
                                    PlatformLogger.error(TAG, "Error parsing message: $text", e)
                                }
                            }

                            is Frame.Close -> {
                                PlatformLogger.debug(TAG, "WebSocket closed by server")
                                throw CancellationException("WebSocket closed by server")
                            }

                            else -> { /* Ignore other frame types */
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    PlatformLogger.error(TAG, "WebSocket cancelled", e)
                    _connectionStatus.emit(ConnectionStatus.DISCONNECTED)

                    // Attempt to reconnect
                    scope.launch {
                        delay(5000)
                        reconnect()
                    }
                } catch (e: Exception) {
                    PlatformLogger.error(TAG, "Error in WebSocket receive loop", e)
                    _connectionStatus.emit(ConnectionStatus.DISCONNECTED)

                    // Attempt to reconnect
                    scope.launch {
                        delay(5000)
                        reconnect()
                    }
                }
            }
        } catch (e: Exception) {
            PlatformLogger.error(TAG, "WebSocket connection failed", e)
            _connectionStatus.emit(ConnectionStatus.FAILED)

            // Attempt to reconnect
            scope.launch {
                delay(5000)
                reconnect()
            }
        }
    }

    /**
     * Send message to WebSocket server
     */
    suspend fun sendCommand(message: GameCommand) {
        try {
            val session = webSocketSession ?: run {
                PlatformLogger.error(TAG, "Attempted to send message with no active session")
                return
            }

            val command = WebSocketCommandMessage(
                type = "COMMAND",
                command = message
            )

            PlatformLogger.debug(TAG, "Sending message: ${Json.encodeToString(command)}")
            session.send(Frame.Text(Json.encodeToString(command)))
        } catch (e: Exception) {
            PlatformLogger.error(TAG, "Failed to send message", e)
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
            PlatformLogger.error(TAG, "Error closing WebSocket", e)
        }
    }

    /**
     * Attempt to reconnect to WebSocket server
     */
    suspend fun reconnect() {
        disconnect()
        connect()
    }

    companion object {
        private val TAG = GameWebSocketClient::class.java.simpleName
    }
}