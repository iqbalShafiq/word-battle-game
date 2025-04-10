package id.usecase.word_battle.websocket

import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.protocol.GameCommand
import id.usecase.word_battle.protocol.GameEvent
import id.usecase.word_battle.protocol.MessageType
import id.usecase.word_battle.protocol.WebSocketMessage
import id.usecase.word_battle.service.GameService
import id.usecase.word_battle.service.MatchmakingService
import id.usecase.word_battle.service.UserService
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

/**
 * Handles WebSocket connections and message routing
 */
class WebSocketController : KoinComponent {
    private val logger = LoggerFactory.getLogger(WebSocketController::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val userService by inject<UserService>()
    private val gameService by inject<GameService>()
    private val matchmakingService by inject<MatchmakingService>()

    /**
     * Handle a new WebSocket connection
     */
    suspend fun handleConnection(socket: DefaultWebSocketSession, playerId: String) {
        try {
            // Get player info
            val player = userService.getPlayerProfile(playerId)
            if (player == null) {
                socket.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid player ID"))
                return
            }

            // Create game player
            val gamePlayer = GamePlayer(
                id = playerId,
                username = player.username
            )

            // Create and register session
            val session = PlayerSession(gamePlayer, socket)
            SessionManager.registerSession(playerId, session)

            // Process incoming messages
            for (frame in socket.incoming) {
                when (frame) {
                    is Frame.Text -> processTextFrame(frame, playerId)
                    is Frame.Ping -> socket.send(Frame.Pong(frame.data))
                    else -> continue
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.info("WebSocket connection for player $playerId closed")
        } catch (e: Exception) {
            logger.error("Error in WebSocket connection for player $playerId: ${e.message}")
        } finally {
            // Clean up
            handleDisconnect(playerId)
        }
    }

    /**
     * Process WebSocket text messages
     */
    private suspend fun processTextFrame(frame: Frame.Text, playerId: String) {
        val text = frame.readText()
        try {
            val message = Json.decodeFromString<WebSocketMessage>(text)

            if (message.type == MessageType.COMMAND) {
                message.command?.let { handleCommand(it) }
            }
        } catch (e: Exception) {
            logger.error("Error processing WebSocket message: ${e.message}")

            // Send error to client
            SessionManager.sendEventToPlayer(
                playerId,
                GameEvent.Error("Invalid message format: ${e.message}")
            )
        }
    }

    /**
     * Handle game commands from clients
     */
    private fun handleCommand(command: GameCommand) {
        coroutineScope.launch {
            when (command) {
                is GameCommand.JoinQueue -> handleJoinQueue(command)
                is GameCommand.LeaveQueue -> handleLeaveQueue(command)
                is GameCommand.SubmitWord -> handleSubmitWord(command)
                is GameCommand.EndRound -> handleEndRound(command)
                is GameCommand.ChatMessage -> handleChatMessage(command)
                is GameCommand.LeaveGame -> handleRequestedDisconnect(command)
            }
        }
    }

    /**
     * Handle player trying to join matchmaking queue
     */
    private suspend fun handleJoinQueue(command: GameCommand.JoinQueue) {
        val position = matchmakingService.addPlayerToQueue(command.playerId, command.gameMode)

        // Notify the player they've joined the queue
        SessionManager.sendEventToPlayer(
            command.playerId,
            GameEvent.QueueJoined(
                playerId = command.playerId,
                position = position,
                estimatedWaitTime = position * 10 // simple estimate: 10 seconds per position
            )
        )

        // Process matchmaking (this might create a game if enough players)
        matchmakingService.processMatchmaking(command.gameMode)
    }

    /**
     * Handle player leaving the matchmaking queue
     */
    private suspend fun handleLeaveQueue(command: GameCommand.LeaveQueue) {
        matchmakingService.removePlayerFromQueue(command.playerId)
    }

    /**
     * Handle word submission from player
     */
    private suspend fun handleSubmitWord(command: GameCommand.SubmitWord) {
        val result = gameService.submitWord(
            command.roundId,
            command.playerId,
            command.word
        )

        // Send result back to the player
        SessionManager.sendEventToPlayer(
            command.playerId,
            GameEvent.WordResult(
                playerId = command.playerId,
                gameId = command.gameId,
                word = command.word,
                isValid = result.isValid,
                score = result.score
            )
        )

        // If valid, broadcast to other players in the game
        if (result.isValid) {
            val otherPlayers = gameService.getOtherPlayersInGame(command.gameId, command.playerId)

            SessionManager.sendEventToPlayers(
                otherPlayers,
                GameEvent.WordResult(
                    playerId = command.playerId,
                    gameId = command.gameId,
                    word = command.word,
                    isValid = true,
                    score = result.score
                )
            )
        }
    }

    /**
     * Handle request to end the current round
     */
    private suspend fun handleEndRound(command: GameCommand.EndRound) {
        gameService.endRound(command.gameId, command.roundId)
    }

    /**
     * Handle chat message from player
     */
    private suspend fun handleChatMessage(command: GameCommand.ChatMessage) {
        val player = userService.getPlayerProfile(command.playerId) ?: return

        // Get all player IDs in the game
        val allPlayers = gameService.getAllPlayersInGame(command.gameId)

        // Create chat event
        val chatEvent = GameEvent.ChatReceived(
            playerId = command.playerId,
            username = player.username,
            message = command.message
        )

        // Send to all players in the game
        SessionManager.sendEventToPlayers(allPlayers, chatEvent)
    }

    /**
     * Handle explicit disconnect request
     */
    private suspend fun handleRequestedDisconnect(command: GameCommand.LeaveGame) {
        handleDisconnect(command.playerId)
    }

    /**
     * Handle player disconnection (cleanup)
     */
    private suspend fun handleDisconnect(playerId: String) {
        // Remove from matchmaking if they're in queue
        matchmakingService.removePlayerFromQueue(playerId)

        // Handle in-game disconnection
        gameService.handlePlayerDisconnect(playerId)

        // Remove session
        SessionManager.removeSession(playerId)
    }
}