package id.usecase.word_battle.network.game

import id.usecase.word_battle.models.GameMode
import kotlinx.serialization.Serializable

@Serializable
data class JoinMatchmakingRequest(
    val gameMode: GameMode = GameMode.CLASSIC
)