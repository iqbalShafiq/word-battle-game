package id.usecase.word_battle.validation

/**
 * Dictionary interface that will have platform-specific implementations
 */
interface Dictionary {
    /** Check if a word exists in the dictionary */
    suspend fun isValidWord(word: String): Boolean

    /** Get word definition if available */
    suspend fun getWordDefinition(word: String): String?
}