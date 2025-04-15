package id.usecase.word_battle.data.repository

import id.usecase.word_battle.PlatformLogger
import id.usecase.word_battle.domain.model.Chat
import id.usecase.word_battle.domain.repository.GameRepository
import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.models.GameRoom
import id.usecase.word_battle.models.Lobby
import id.usecase.word_battle.network.WebSocketManager
import id.usecase.word_battle.protocol.GameCommand
import id.usecase.word_battle.protocol.GameEvent
import id.usecase.word_battle.protocol.GameStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameRepositoryImpl(
    private val webSocketManager: WebSocketManager,
    scope: CoroutineScope
) : GameRepository {

    private val lobby = MutableStateFlow<Lobby?>(null)
    private val gameRoom = MutableStateFlow<GameRoom?>(null)
    private val chatRoom = MutableStateFlow<List<Chat>>(emptyList())

    init {
        scope.launch {
            webSocketManager
                .getIncomingMessages()
                ?.collectLatest { event ->
                    when (event) {
                        // error handling soon
                        is GameEvent.Error -> TODO()

                        // Player joined the queue
                        is GameEvent.QueueJoined -> {
                            lobby.update { Lobby(estimatedTime = event.estimatedWaitTime) }
                        }

                        // Game has created
                        is GameEvent.GameCreated -> {
                            val game = GameRoom(
                                id = event.gameId,
                                gamePlayers = event.players,
                                state = GameStatus.GAME_CREATED
                            )

                            gameRoom.update {
                                GameRoom(
                                    id = game.id,
                                    gamePlayers = game.gamePlayers,
                                    state = game.state
                                )
                            }
                        }

                        // New round has started
                        is GameEvent.RoundStarted -> {
                            gameRoom.update {
                                it?.copy(
                                    state = GameStatus.ROUND_ACTIVE,
                                    currentRound = event.round.roundNumber,
                                    currentRoundId = event.round.id,
                                    currentLetters = event.round.letters,
                                    remainingRoundTime = 60
                                )
                            }
                        }

                        // Chat message received
                        is GameEvent.ChatReceived -> {
                            val receivedChat = Chat(
                                playerId = event.playerId,
                                message = event.message,
                                timestamp = event.timestamp
                            )

                            chatRoom.update { it + receivedChat }
                        }

                        // Player score updated
                        is GameEvent.WordResult -> {
                            gameRoom.update {
                                it?.copy(
                                    gamePlayers = it.gamePlayers.map { player ->
                                        if (player.id == event.playerId) {
                                            player.copy(
                                                score = player.score + event.score
                                            )
                                        } else {
                                            player
                                        }
                                    }
                                )
                            }
                        }

                        // Round ended
                        is GameEvent.RoundEnded -> {
                            gameRoom.update { it?.copy(state = GameStatus.ROUND_OVER) }
                        }

                        // Game over
                        is GameEvent.GameEnded -> {
                            gameRoom.update { it?.copy(state = GameStatus.GAME_OVER) }
                        }
                    }
                }
        }
    }

    override suspend fun joinMatchmaking(playerId: String, gameMode: GameMode) {
        webSocketManager.connect()
        webSocketManager.sendCommand(
            message = GameCommand.JoinQueue(
                playerId = playerId,
                gameMode = gameMode
            )
        )
    }

    override suspend fun cancelMatchmaking(playerId: String) {
        gameRoom.value = null
        webSocketManager.sendCommand(
            message = GameCommand.LeaveQueue(playerId)
        )
    }

    override suspend fun leaveGame(playerId: String) {
        gameRoom.value = null
    }

    override suspend fun submitWord(
        playerId: String,
        gameId: String,
        roundId: String,
        word: String
    ) {
        webSocketManager.sendCommand(
            message = GameCommand.SubmitWord(
                playerId = playerId,
                gameId = gameId,
                roundId = roundId,
                word = word
            )
        )
    }

    override fun observeLobby(): Flow<Lobby?> {
        return lobby.map { it }
    }

    override fun observeGameRoom(): Flow<GameRoom?> {
        return gameRoom.map { it }
    }

    override fun observeChatRoom(): Flow<List<Chat>> {
        return chatRoom.map { it }
    }
}