package id.usecase.word_battle.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import id.usecase.word_battle.PlatformLogger
import id.usecase.word_battle.network.ConnectionStatus
import id.usecase.word_battle.network.GameWebSocketClient
import id.usecase.word_battle.protocol.GameCommand
import id.usecase.word_battle.protocol.GameEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Background service for maintaining WebSocket connection
 */
class GameWebSocketService : Service() {

    private val binder = WebSocketBinder()
    private val webSocketClient: GameWebSocketClient by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Connection state
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Access to incoming messages from WebSocket
    val incomingMessages: SharedFlow<GameEvent> = webSocketClient.incomingEvent

    private var reconnectJob: Job? = null
    private var connectionMonitorJob: Job? = null
    private var reconnectAttempts = 0
    private val isReconnecting = AtomicBoolean(false)
    private var bufferMessage: MutableList<GameCommand> = mutableListOf()

    override fun onCreate() {
        super.onCreate()
        PlatformLogger.debug(TAG, "Service created")

        // Start monitoring connection
        startConnectionMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        PlatformLogger.debug(TAG, "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        PlatformLogger.debug(TAG, "Service destroyed")
        disconnect()
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Connect to the WebSocket server
     */
    fun connect() {
        if (
            _connectionState.value == ConnectionState.CONNECTED ||
            _connectionState.value == ConnectionState.CONNECTING
        ) {
            Log.w(TAG, "Already connected or connecting")
            return
        }
        PlatformLogger.debug(TAG, "Attempting to connect to WebSocket server")

        serviceScope.launch {
            try {
                webSocketClient.connect()
                PlatformLogger.debug(TAG, "Connected to WebSocket server")
                reconnectAttempts = 0
            } catch (e: Exception) {
                PlatformLogger.error(TAG, "Failed to connect to WebSocket", e)
                handleConnectionFailure()
            }
        }
    }

    /**
     * Disconnect from the WebSocket server
     */
    fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null

        serviceScope.launch {
            try {
                webSocketClient.disconnect()
            } catch (e: Exception) {
                PlatformLogger.error(TAG, "Error disconnecting WebSocket", e)
            }
        }
    }

    /**
     * Send a message through the WebSocket
     */
    suspend fun sendMessage(message: GameCommand): Boolean {
        return try {
            if (_connectionState.value != ConnectionState.CONNECTED) {
                bufferMessage.add(message)
                Log.w(TAG, "Attempted to send message while not connected")
                return false
            }

            webSocketClient.sendCommand(message)
            true
        } catch (e: Exception) {
            PlatformLogger.error(TAG, "Failed to send message", e)
            handleConnectionFailure()
            false
        }
    }

    /**
     * Handle connection failure and attempt reconnection
     */
    private fun handleConnectionFailure() {
        if (isReconnecting.getAndSet(true)) return
        reconnectJob?.cancel()

        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.w(TAG, "Max reconnection attempts reached")
            isReconnecting.set(false)
            return
        }

        reconnectJob = serviceScope.launch {
            try {
                reconnectAttempts++

                val delayTime = RECONNECT_DELAY_MS * reconnectAttempts
                PlatformLogger.debug(
                    TAG,
                    "Attempting reconnect in $delayTime ms (attempt $reconnectAttempts)"
                )

                delay(delayTime)
                webSocketClient.reconnect()
                reconnectAttempts = 0
            } catch (e: Exception) {
                PlatformLogger.error(TAG, "Reconnection attempt failed", e)
            } finally {
                isReconnecting.set(false)
            }
        }
    }

    /**
     * Start monitoring the WebSocket connection status
     */
    private fun startConnectionMonitoring() {
        connectionMonitorJob?.cancel()
        connectionMonitorJob = serviceScope.launch {
            webSocketClient.connectionStatus.collectLatest { status ->
                when (status) {
                    ConnectionStatus.CONNECTED -> {
                        _connectionState.value = ConnectionState.CONNECTED
                        if (bufferMessage.isNotEmpty()) {
                            bufferMessage.forEach { message ->
                                sendMessage(message)
                            }
                            bufferMessage.clear()
                        }
                        reconnectAttempts = 0
                    }

                    ConnectionStatus.CONNECTING -> {
                        _connectionState.value = ConnectionState.CONNECTING
                    }

                    ConnectionStatus.DISCONNECTED -> {
                        if (_connectionState.value == ConnectionState.CONNECTED) {
                            _connectionState.value = ConnectionState.DISCONNECTED
                            handleConnectionFailure()
                        }
                    }

                    ConnectionStatus.FAILED -> {
                        _connectionState.value = ConnectionState.FAILED
                    }
                }
            }
        }
    }

    /**
     * Service binder for clients to interact with the service
     */
    inner class WebSocketBinder : Binder() {
        fun getService(): GameWebSocketService = this@GameWebSocketService
    }

    /**
     * Connection states
     */
    enum class ConnectionState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
        FAILED
    }

    companion object {
        private val TAG = GameWebSocketService::class.java.simpleName
        private const val RECONNECT_DELAY_MS = 3000L
        private const val MAX_RECONNECT_ATTEMPTS = 5
    }
}