package id.usecase.word_battle.network.game

import id.usecase.word_battle.models.GameMode
import kotlinx.serialization.Serializable


@Serializable
data class CreateGameRequest(
    val playerIds: List<String>,
    val gameMode: GameMode = GameMode.CLASSIC
)
