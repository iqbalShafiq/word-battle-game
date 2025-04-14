package id.usecase.word_battle.ui.screens.auth

import id.usecase.word_battle.domain.repository.AuthRepository
import id.usecase.word_battle.mvi.MviViewModel
import kotlinx.coroutines.delay

/**
 * Login states
 */
data class LoginState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null
)

/**
 * Login intents
 */
sealed class LoginIntent {
    data class UpdateUsername(val username: String) : LoginIntent()
    data class UpdatePassword(val password: String) : LoginIntent()
    object Login : LoginIntent()
    object NavigateToRegister : LoginIntent()
}

/**
 * Login side effects
 */
sealed class LoginEffect {
    object NavigateToHome : LoginEffect()
    object NavigateToRegister : LoginEffect()
    data class ShowError(val message: String) : LoginEffect()
}

/**
 * LoginViewModel implementing MVI pattern
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : MviViewModel<LoginIntent, LoginState, LoginEffect>(LoginState()) {

    override suspend fun handleIntent(intent: LoginIntent, state: LoginState) {
        when (intent) {
            is LoginIntent.UpdateUsername -> {
                updateState { copy(username = intent.username, usernameError = null) }
            }

            is LoginIntent.UpdatePassword -> {
                updateState { copy(password = intent.password, passwordError = null) }
            }

            is LoginIntent.Login -> {
                if (validateInputs()) {
                    performLogin()
                }
            }

            is LoginIntent.NavigateToRegister -> {
                sendEffect(LoginEffect.NavigateToRegister)
            }
        }
    }

    private fun validateInputs(): Boolean {
        val username = state.value.username
        val password = state.value.password

        var isValid = true

        if (username.isBlank()) {
            updateState { copy(usernameError = "Username cannot be empty") }
            isValid = false
        }

        if (password.isBlank()) {
            updateState { copy(passwordError = "Password cannot be empty") }
            isValid = false
        } else if (password.length < 6) {
            updateState { copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        return isValid
    }

    private suspend fun performLogin() {
        updateState { copy(isLoading = true) }

        try {
            val response = authRepository.login(
                username = state.value.username,
                password = state.value.password
            )
            if (response.isFailure) throw Exception("Auth failed")
            sendEffect(LoginEffect.NavigateToHome)
        } catch (e: Exception) {
            sendEffect(LoginEffect.ShowError(e.message ?: "Login failed"))
        } finally {
            updateState { copy(isLoading = false) }
        }
    }
}