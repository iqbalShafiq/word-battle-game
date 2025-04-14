package id.usecase.word_battle.network

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import id.usecase.word_battle.PlatformLogger
import id.usecase.word_battle.protocol.GameCommand
import id.usecase.word_battle.protocol.GameEvent
import id.usecase.word_battle.service.GameWebSocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Manager for WebSocket connection that handles binding to background service
 */
class WebSocketManager(private val context: Context) {
    private var webSocketService: GameWebSocketService? = null
    private var bound = false

    // Message buffer when service not yet bound
    private val messageBuffer = mutableListOf<GameCommand>()

    // Events for connection state
    private val _events = MutableSharedFlow<WebSocketEvent>(extraBufferCapacity = 10)
    val events = _events.asSharedFlow()

    /**
     * Connection to the WebSocket service
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as GameWebSocketService.WebSocketBinder
            webSocketService = binder.getService()
            bound = true

            Log.d(TAG, "Service connected")
            _events.tryEmit(WebSocketEvent.ServiceConnected)

            // Send any buffered messages
            flushMessageBuffer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            webSocketService = null
            bound = false

            Log.d(TAG, "Service disconnected")
            _events.tryEmit(WebSocketEvent.ServiceDisconnected)
        }
    }

    /**
     * Start and bind to the WebSocket service
     */
    fun startService() {
        Log.d(TAG, "Starting WebSocket service...")

        val intent = Intent(context, GameWebSocketService::class.java)
        context.startService(intent)

        Log.d(TAG, "Binding to WebSocket service...")
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Stop and unbind from the WebSocket service
     */
    fun stopService() {
        if (bound) {
            webSocketService?.disconnect()
            context.unbindService(serviceConnection)
            bound = false
        }

        val intent = Intent(context, GameWebSocketService::class.java)
        context.stopService(intent)
    }

    /**
     * Connect to the WebSocket server
     */
    fun connect() {
        if (bound) {
            webSocketService?.connect()
        } else {
            Log.w(TAG, "Cannot connect, service not bound")
            startService()
        }
    }

    /**
     * Disconnect from the WebSocket server
     */
    fun disconnect() {
        webSocketService?.disconnect()
    }

    /**
     * Send a message through the WebSocket
     */
    suspend fun sendCommand(message: GameCommand): Boolean {
        val service = webSocketService
        return if (service != null) {
            service.sendMessage(message)
        } else {
            messageBuffer.add(message)
            Log.d(TAG, "Message buffered until service is connected")
            false
        }
    }

    /**
     * Get the flow of incoming messages
     */
    fun getIncomingMessages(): Flow<GameEvent>? {
        return webSocketService?.incomingMessages
    }

    /**
     * Get the connection state flow
     */
    fun getConnectionState(): StateFlow<GameWebSocketService.ConnectionState>? {
        return webSocketService?.connectionState
    }

    /**
     * Flush any buffered messages
     */
    private fun flushMessageBuffer() {
        if (!bound || messageBuffer.isEmpty()) return

        val service = webSocketService ?: return

        CoroutineScope(Dispatchers.IO).launch {
            messageBuffer.forEach { message ->
                service.sendMessage(message)
            }
            messageBuffer.clear()
        }
    }

    /**
     * WebSocket events
     */
    sealed class WebSocketEvent {
        object ServiceConnected : WebSocketEvent()
        object ServiceDisconnected : WebSocketEvent()
    }

    companion object {
        private val TAG = WebSocketManager::class.java.simpleName
    }
}