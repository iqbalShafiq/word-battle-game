package id.usecase.word_battle.data.repository

import id.usecase.word_battle.data.models.word.Word

/**
 * Repository for dictionary word operations
 */
interface WordRepository {
    /**
     * Check if a word is valid according to the dictionary
     */
    suspend fun isValidWord(word: String): Boolean

    /**
     * Add a word to the dictionary
     */
    suspend fun addWord(word: String, isValid: Boolean = true): Word?

    /**
     * Get random word set with specified length
     */
    suspend fun getRandomWordSet(length: Int, count: Int = 1): List<Word>

    /**
     * Generate random letters for a game round
     * (typically 7-10 letters with a good mix of vowels and consonants)
     */
    suspend fun generateRandomLetters(length: Int = 8): String
}