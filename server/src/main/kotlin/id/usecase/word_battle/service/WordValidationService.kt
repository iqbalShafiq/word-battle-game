package id.usecase.word_battle.service

import id.usecase.word_battle.data.repository.WordRepository
import id.usecase.word_battle.network.game.WordValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

/**
 * Service for validating words in the game
 */
class WordValidationService(private val wordRepository: WordRepository) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Validate if a word is acceptable for the game
     * @param word The word to validate
     * @param availableLetters Letters available to form the word
     * @param minimumLength Minimum word length (default: 3)
     * @return Validation result with reason
     */
    suspend fun validateWord(
        word: String,
        availableLetters: String,
        minimumLength: Int = 3
    ): WordValidationResult = withContext(Dispatchers.Default) {
        val cleanWord = word.trim().lowercase()

        // Check minimum length
        if (cleanWord.length < minimumLength) {
            return@withContext WordValidationResult(
                isValid = false,
                reason = "Word too short (minimum $minimumLength letters)"
            )
        }

        // Check if word is in dictionary
        if (!wordRepository.isValidWord(cleanWord)) {
            return@withContext WordValidationResult(
                isValid = false,
                reason = "Word not found in dictionary"
            )
        }

        // Check if word can be formed from available letters
        if (!canFormWord(cleanWord, availableLetters)) {
            return@withContext WordValidationResult(
                isValid = false,
                reason = "Word cannot be formed with the available letters"
            )
        }

        // All checks passed
        WordValidationResult(
            isValid = true,
            reason = "Valid word"
        )
    }

    /**
     * Check if a word can be formed from available letters
     */
    private fun canFormWord(word: String, availableLetters: String): Boolean {
        // Count available letters
        val letterCounts = availableLetters.lowercase()
            .groupingBy { it }
            .eachCount()
            .toMutableMap()

        // Check each letter in the word
        for (char in word) {
            val count = letterCounts[char] ?: 0
            if (count <= 0) {
                return false
            }
            letterCounts[char] = count - 1
        }

        return true
    }

    /**
     * Suggest valid words that can be formed from the given letters
     * Useful for testing and debugging
     */
    suspend fun suggestValidWords(
        availableLetters: String,
        maxSuggestions: Int = 10,
        minLength: Int = 3
    ): List<String> {
        // Get a list of words from the dictionary
        val allWords = wordRepository.getRandomWords(1000)

        // Filter for words that can be formed with the available letters
        return allWords
            .filter { it.length >= minLength && canFormWord(it, availableLetters) }
            .take(maxSuggestions)
    }
}