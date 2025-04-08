package id.usecase.word_battle.ui.screens.auth

import id.usecase.word_battle.domain.repository.AuthRepository
import id.usecase.word_battle.mvi.MviViewModel

/**
 * Register states
 */
data class RegisterState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

/**
 * Register intents
 */
sealed class RegisterIntent {
    data class UpdateUsername(val username: String) : RegisterIntent()
    data class UpdatePassword(val password: String) : RegisterIntent()
    data class UpdateConfirmPassword(val confirmPassword: String) : RegisterIntent()
    object Register : RegisterIntent()
    object NavigateBack : RegisterIntent()
}

/**
 * Register side effects
 */
sealed class RegisterEffect {
    object NavigateToHome : RegisterEffect()
    object NavigateBack : RegisterEffect()
    data class ShowError(val message: String) : RegisterEffect()
}

/**
 * RegisterViewModel implementing MVI pattern
 */
class RegisterViewModel(
    private val authRepository: AuthRepository
) : MviViewModel<RegisterIntent, RegisterState, RegisterEffect>(RegisterState()) {

    override suspend fun handleIntent(intent: RegisterIntent, state: RegisterState) {
        when (intent) {
            is RegisterIntent.UpdateUsername -> {
                updateState { copy(username = intent.username, usernameError = null) }
            }

            is RegisterIntent.UpdatePassword -> {
                updateState { copy(password = intent.password, passwordError = null) }
            }

            is RegisterIntent.UpdateConfirmPassword -> {
                updateState { copy(
                    confirmPassword = intent.confirmPassword,
                    confirmPasswordError = null
                ) }
            }

            is RegisterIntent.Register -> {
                if (validateInputs()) {
                    performRegister()
                }
            }

            is RegisterIntent.NavigateBack -> {
                sendEffect(RegisterEffect.NavigateBack)
            }
        }
    }

    private fun validateInputs(): Boolean {
        val username = state.value.username
        val password = state.value.password
        val confirmPassword = state.value.confirmPassword

        var isValid = true

        if (username.isBlank()) {
            updateState { copy(usernameError = "Username cannot be empty") }
            isValid = false
        } else if (username.length < 3) {
            updateState { copy(usernameError = "Username must be at least 3 characters") }
            isValid = false
        }

        if (password.isBlank()) {
            updateState { copy(passwordError = "Password cannot be empty") }
            isValid = false
        } else if (password.length < 6) {
            updateState { copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        if (confirmPassword != password) {
            updateState { copy(confirmPasswordError = "Passwords don't match") }
            isValid = false
        }

        return isValid
    }

    private suspend fun performRegister() {
        updateState { copy(isLoading = true) }

        try {
            val result = authRepository.register(
                username = state.value.username,
                password = state.value.password
            )

            result.onSuccess {
                sendEffect(RegisterEffect.NavigateToHome)
            }.onFailure { error ->
                sendEffect(RegisterEffect.ShowError(error.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            sendEffect(RegisterEffect.ShowError(e.message ?: "Registration failed"))
        } finally {
            updateState { copy(isLoading = false) }
        }
    }
}