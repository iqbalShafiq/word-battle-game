package id.usecase.word_battle.ui.models

/**
 * UI models for presentation layer - optimized for display
 */
data class UserUi(
    val id: String,
    val username: String,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalScore: Int = 0
)

data class PlayerUi(
    val id: String,
    val username: String,
    val score: Int = 0,
    val isCurrentPlayer: Boolean = false,
    val isConnected: Boolean = true
)

data class GameUi(
    val id: String,
    val players: List<PlayerUi> = emptyList(),
    val currentRound: Int = 1,
    val maxRounds: Int = 3,
    val status: GameStatusUi = GameStatusUi.WAITING,
    val letters: List<Char> = emptyList()
)

enum class GameStatusUi {
    WAITING,
    STARTING,
    WAITING_FOR_PLAYERS,
    ROUND_END,
    FINISHED
}