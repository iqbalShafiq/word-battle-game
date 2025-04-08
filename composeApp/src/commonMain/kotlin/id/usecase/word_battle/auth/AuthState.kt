package id.usecase.word_battle.auth

/**
 * Authentication states
 */
sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}