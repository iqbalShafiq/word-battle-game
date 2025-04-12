package id.usecase.word_battle.ui.screens.lobby

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Combined lobby and waiting room for cleaner navigation
 */
@Composable
fun LobbyWithWaitingRoom(
    onGameReady: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showWaitingRoom by remember { mutableStateOf(false) }
    var gameId by remember { mutableStateOf("") }
    var playerCount by remember { mutableIntStateOf(1) }

    // Simulated player joining for demo
    LaunchedEffect(showWaitingRoom, gameId) {
        if (showWaitingRoom) {
            // Simulate other player joining after 3 seconds
            kotlinx.coroutines.delay(3000)
            playerCount = 2
        }
    }

    // Show lobby screen
    LobbyScreen(
        onGameFound = { foundGameId ->
            gameId = foundGameId
            showWaitingRoom = true
        },
        onNavigateBack = onNavigateBack
    )

    // Show waiting room dialog when game is found
    if (showWaitingRoom) {
        WaitingRoomDialog(
            gameId = gameId,
            playerCount = playerCount,
            maxPlayers = 2,
            onCancel = {
                showWaitingRoom = false
                onNavigateBack()
            },
            onGameStarting = {
                onGameReady(gameId)
            }
        )
    }
}