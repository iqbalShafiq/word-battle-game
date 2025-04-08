package id.usecase.word_battle.ui.screens.profile

import id.usecase.word_battle.domain.repository.AuthRepository
import id.usecase.word_battle.mvi.MviViewModel
import id.usecase.word_battle.ui.models.UserUi

/**
 * Profile state
 */
data class ProfileState(
    val user: UserUi? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Profile intents
 */
sealed class ProfileIntent {
    object LoadProfile : ProfileIntent()
    object NavigateBack : ProfileIntent()
}

/**
 * Profile effects
 */
sealed class ProfileEffect {
    object NavigateBack : ProfileEffect()
    data class ShowError(val message: String) : ProfileEffect()
}

/**
 * Profile ViewModel
 */
class ProfileViewModel(
    private val authRepository: AuthRepository
) : MviViewModel<ProfileIntent, ProfileState, ProfileEffect>(ProfileState()) {

    override suspend fun handleIntent(intent: ProfileIntent, state: ProfileState) {
        when (intent) {
            is ProfileIntent.LoadProfile -> {
                loadProfile()
            }

            is ProfileIntent.NavigateBack -> {
                sendEffect(ProfileEffect.NavigateBack)
            }
        }
    }

    private suspend fun loadProfile() {
        updateState { copy(isLoading = true, error = null) }

        try {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                // Map to UI model
                val userUi = UserUi(
                    id = user.id,
                    username = user.username,
                    gamesPlayed = user.stats.gamesPlayed,
                    gamesWon = user.stats.gamesWon,
                    totalScore = user.stats.totalScore
                )

                updateState { copy(user = userUi, isLoading = false) }
            } else {
                updateState { copy(error = "Could not load user profile", isLoading = false) }
            }
        } catch (e: Exception) {
            updateState { copy(error = e.message ?: "Failed to load profile", isLoading = false) }
        }
    }
}