package id.usecase.word_battle.data.repository

import id.usecase.word_battle.domain.model.Chat
import id.usecase.word_battle.domain.repository.GameRepository
import id.usecase.word_battle.models.GameMode
import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.models.GameRoom
import id.usecase.word_battle.models.GameState
import id.usecase.word_battle.models.Lobby
import id.usecase.word_battle.network.GameWebSocketClient
import id.usecase.word_battle.protocol.GameCommand
import id.usecase.word_battle.protocol.GameEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameRepositoryImpl(
    private val webSocketClient: GameWebSocketClient,
    scope: CoroutineScope
) : GameRepository {

    private val lobby = MutableStateFlow<Lobby?>(null)
    private val gameRoom = MutableStateFlow<GameRoom?>(null)
    private val chatRoom = MutableStateFlow<List<Chat>>(emptyList())

    init {
        scope.launch {
            // Player joined the queue
            webSocketClient.incomingCommand
                .filter { it is GameEvent.QueueJoined }
                .map { it as GameEvent.QueueJoined }
                .collect { queue ->
                    lobby.update { Lobby(estimatedTime = queue.estimatedWaitTime) }
                }

            // Game has created
            webSocketClient.incomingCommand
                .filter { it is GameEvent.GameCreated }
                .map {
                    val event = it as GameEvent.GameCreated
                    GameRoom(
                        id = event.gameId,
                        gamePlayers = event.players,
                        state = GameState.STARTING
                    )
                }
                .collect { game ->
                    gameRoom.update {
                        GameRoom(
                            id = game.id,
                            gamePlayers = game.gamePlayers,
                            state = game.state
                        )
                    }
                }

            // New round has started
            webSocketClient.incomingCommand
                .filter { it is GameEvent.RoundStarted }
                .map { it as GameEvent.RoundStarted }
                .collect { round ->
                    gameRoom.update {
                        it?.copy(
                            currentRound = round.round.roundNumber,
                            currentLetters = round.round.letters
                        )
                    }
                }

            // Chat message received
            webSocketClient.incomingCommand
                .filter { it is GameEvent.ChatReceived }
                .map {
                    val receivedChat = it as GameEvent.ChatReceived
                    Chat(
                        playerId = receivedChat.playerId,
                        message = receivedChat.message,
                        timestamp = receivedChat.timestamp
                    )
                }
                .collect { chat -> chatRoom.update { it + chat } }

            // Player score updated
            webSocketClient.incomingCommand
                .filter { it is GameEvent.WordResult }
                .map { it as GameEvent.WordResult }
                .collect { result ->
                    gameRoom.update {
                        it?.copy(
                            gamePlayers = it.gamePlayers.map { player ->
                                if (player.id == result.playerId) {
                                    player.copy(
                                        score = player.score + result.score
                                    )
                                } else {
                                    player
                                }
                            }
                        )
                    }
                }

            // Round ended
            webSocketClient.incomingCommand
                .filter { it is GameEvent.RoundEnded }
                .map { it as GameEvent.RoundEnded }
                .collect { gameRoom.update { it?.copy(state = GameState.ROUND_ENDING) } }

            // Game over
            webSocketClient.incomingCommand
                .filter { it is GameEvent.GameEnded }
                .map { it as GameEvent.GameEnded }
                .collect { gameRoom.update { it?.copy(state = GameState.GAME_OVER) } }
        }
    }

    override suspend fun joinMatchmaking(playerId: String, gameMode: GameMode) {
        webSocketClient.sendCommand(
            message = GameCommand.JoinQueue(
                playerId = playerId,
                gameMode = gameMode
            )
        )
    }

    override suspend fun cancelMatchmaking(playerId: String) {
        gameRoom.value = null
        webSocketClient.sendCommand(
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
        webSocketClient.sendCommand(
            message = GameCommand.SubmitWord(
                playerId = playerId,
                gameId = gameId,
                roundId = roundId,
                word = word
            )
        )
    }

    override fun observeGameState(): Flow<GameRoom> {
        return gameRoom.map { it as GameRoom }
    }

    override fun observePlayers(): Flow<List<GamePlayer>> {
        return gameRoom.map { it?.gamePlayers ?: emptyList() }
    }

    override fun observeChatRoom(): Flow<List<Chat>> {
        return chatRoom.map { it }
    }
}