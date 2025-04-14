package id.usecase.word_battle.lifecycle

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import id.usecase.word_battle.network.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Observes app lifecycle events to manage WebSocket connections
 */
class AppLifecycleObserver(
    private val webSocketManager: WebSocketManager
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Track if we're in a game when going to background
    private var wasInGame = false

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "App moved to foreground")

        scope.launch {
            if (wasInGame) {
                // If we were in a game when the app went to background,
                // ensure the WebSocket is connected
                Log.d(TAG, "Reconnecting WebSocket in foreground")
                webSocketManager.connect()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "App moved to background")

        // We'll keep the WebSocket service running in the background
        // but check if we're actively in a game
        wasInGame = false // Logic to check if in game would go here
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "App destroyed")

        scope.launch {
            webSocketManager.stopService()
        }

        scope.cancel()
    }

    /**
     * Set if the user is currently in an active game
     */
    fun setInGame(inGame: Boolean) {
        this.wasInGame = inGame
    }

    companion object {
        private val TAG = AppLifecycleObserver::class.java.simpleName
    }
}