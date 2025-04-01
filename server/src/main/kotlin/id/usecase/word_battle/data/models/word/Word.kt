package id.usecase.word_battle.data.models.word

import kotlinx.serialization.Serializable

/**
 * Represents a word in the dictionary
 */
@Serializable
data class Word(
    val id: Int = 0,
    val word: String,
    val isValid: Boolean = true,
    val length: Int = word.length
)