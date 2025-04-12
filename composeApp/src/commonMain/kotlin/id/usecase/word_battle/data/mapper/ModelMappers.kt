package id.usecase.word_battle.data.mapper

import id.usecase.word_battle.models.GamePlayer
import id.usecase.word_battle.models.GameRoom
import id.usecase.word_battle.models.UserProfile
import id.usecase.word_battle.protocol.GameStatus
import id.usecase.word_battle.ui.models.GameStatusUi
import id.usecase.word_battle.ui.models.GameUi
import id.usecase.word_battle.ui.models.PlayerUi
import id.usecase.word_battle.ui.models.UserUi

/**
 * Mapper functions to convert between shared models and UI models
 */
fun UserProfile.toUiModel(): UserUi {
    return UserUi(
        id = this.id,
        username = this.username,
        gamesPlayed = this.stats.gamesPlayed,
        gamesWon = this.stats.gamesWon,
        totalScore = this.stats.totalScore
    )
}

fun GamePlayer.toUiModel(isCurrentPlayer: Boolean = false): PlayerUi {
    return PlayerUi(
        id = this.id,
        username = this.username,
        score = this.score,
        isCurrentPlayer = isCurrentPlayer,
        isConnected = this.isActive
    )
}

fun GameStatus.toUiModel(): GameStatusUi {
    return when (this) {
        GameStatus.ROUND_ACTIVE -> GameStatusUi.WAITING
        GameStatus.WAITING -> GameStatusUi.WAITING_FOR_PLAYERS
        GameStatus.GAME_CREATED -> GameStatusUi.STARTING
        GameStatus.ROUND_OVER -> GameStatusUi.ROUND_END
        GameStatus.GAME_OVER -> GameStatusUi.FINISHED
    }
}

fun GameRoom.toUiModel(currentUserId: String): GameUi {
    return GameUi(
        id = this.id,
        players = this.gamePlayers.map { it.toUiModel(it.id == currentUserId) },
        currentRound = this.currentRound,
        maxRounds = this.maxRounds,
        status = this.state.toUiModel(),
        letters = this.currentLetters.map { it.toChar() },
    )
}