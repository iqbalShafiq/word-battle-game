package id.usecase.word_battle.domain.model

data class Chat(
    val playerId: String,
    val message:String,
    val timestamp: Long,
)
