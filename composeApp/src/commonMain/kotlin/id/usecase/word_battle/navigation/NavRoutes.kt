package id.usecase.word_battle.navigation

import kotlinx.serialization.Serializable

@Serializable
internal object Splash

@Serializable
internal object Login

@Serializable
internal object Register

@Serializable
internal object Home

@Serializable
internal object Lobby

@Serializable
internal data class Game(val gameId: String)

@Serializable
internal object Profile

@Serializable
internal object Settings

@Serializable
internal object ComponentsDemo