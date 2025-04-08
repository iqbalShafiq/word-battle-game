package id.usecase.word_battle.ui.screens.home

import id.usecase.word_battle.domain.model.User
import id.usecase.word_battle.domain.repository.AuthRepository
import id.usecase.word_battle.domain.repository.GameRepository
import id.usecase.word_battle.mvi.MviViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Home screen state
 */
data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = true
)

/**
 * Home screen intents
 */
sealed class HomeIntent {
    object LoadUserData : HomeIntent()
    object Logout : HomeIntent()
    object NavigateToLobby : HomeIntent()
    object NavigateToProfile : HomeIntent()
}

/**
 * Home screen effects
 */
sealed class HomeEffect {
    object NavigateToLobby : HomeEffect()
    object NavigateToProfile : HomeEffect()
    object NavigateToLogin : HomeEffect()
    data class ShowError(val message: String) : HomeEffect()
}

/**
 * Home screen ViewModel
 */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : MviViewModel<HomeIntent, HomeState, HomeEffect>(HomeState()) {

    init {
        processIntent(HomeIntent.LoadUserData)
    }

    override suspend fun handleIntent(intent: HomeIntent, state: HomeState) {
        when (intent) {
            is HomeIntent.LoadUserData -> {
                updateState { copy(isLoading = true) }

                try {
                    val user = authRepository.getCurrentUser()
                    updateState { copy(user = user, isLoading = false) }
                } catch (e: Exception) {
                    updateState { copy(isLoading = false) }
                    sendEffect(HomeEffect.ShowError(e.message ?: "Failed to load user data"))
                }
            }

            is HomeIntent.Logout -> {
                updateState { copy(isLoading = true) }

                try {
                    authRepository.logout()
                    sendEffect(HomeEffect.NavigateToLogin)
                } catch (e: Exception) {
                    updateState { copy(isLoading = false) }
                    sendEffect(HomeEffect.ShowError(e.message ?: "Failed to logout"))
                }
            }

            is HomeIntent.NavigateToLobby -> {
                sendEffect(HomeEffect.NavigateToLobby)
            }

            is HomeIntent.NavigateToProfile -> {
                sendEffect(HomeEffect.NavigateToProfile)
            }
        }
    }
}