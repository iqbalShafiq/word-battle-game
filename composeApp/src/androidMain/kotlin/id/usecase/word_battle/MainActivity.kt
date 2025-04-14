package id.usecase.word_battle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import id.usecase.word_battle.navigation.NavGraph
import id.usecase.word_battle.network.WebSocketManager
import id.usecase.word_battle.service.GameWebSocketService
import id.usecase.word_battle.ui.components.NetworkStatusBar
import id.usecase.word_battle.ui.screens.splash.SplashScreen
import id.usecase.word_battle.ui.theme.WordBattleTheme
import id.usecase.word_battle.utils.NetworkMonitor
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val webSocketManager: WebSocketManager by inject()
    private val networkMonitor: NetworkMonitor by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        // Start WebSocket service
        webSocketManager.startService()
        PlatformLogger.debug("TAG", "Test!")

        setContent {
            WordBattleApp(
                networkMonitor = networkMonitor,
                webSocketManager = webSocketManager
            )
        }
    }
}

@Composable
fun WordBattleApp(
    networkMonitor: NetworkMonitor,
    webSocketManager: WebSocketManager
) {
    // Observe network status
    val isNetworkConnected by networkMonitor.isOnline()
        .collectAsState(initial = true)

    // Observe WebSocket connection
    val webSocketState by webSocketManager.getConnectionState()
        ?.collectAsState(initial = GameWebSocketService.ConnectionState.DISCONNECTED)
        ?: androidx.compose.runtime.remember { mutableStateOf(GameWebSocketService.ConnectionState.DISCONNECTED) }

    val isWebSocketConnected = webSocketState == GameWebSocketService.ConnectionState.CONNECTED

    WordBattleTheme(darkTheme = isSystemInDarkTheme()) {
        Scaffold(
            topBar = {
                NetworkStatusBar(
                    isConnected = isNetworkConnected,
                    isWebSocketConnected = isWebSocketConnected
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                Box {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WordBattleTheme {
        SplashScreen(onNavigateToLogin = {}, onNavigateToHome = {})
    }
}