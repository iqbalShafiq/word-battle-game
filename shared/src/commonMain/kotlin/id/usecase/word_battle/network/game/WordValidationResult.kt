package id.usecase.word_battle.network.game

/**
 * Result of word validation
 */
data class WordValidationResult(
    val isValid: Boolean,
    val reason: String
)