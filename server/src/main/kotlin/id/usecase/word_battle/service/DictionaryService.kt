package id.usecase.word_battle.service

import id.usecase.word_battle.data.models.word.Word
import id.usecase.word_battle.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Service for dictionary-related operations
 */
class DictionaryService(private val wordRepository: WordRepository) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Initialize the dictionary by loading words from a file
     */
    suspend fun initializeDictionary(filePath: String = "/words_indonesia.txt"): Int {
        var count = 0

        try {
            // Load words from the resource file
            withContext(Dispatchers.IO) {
                val inputStream = javaClass.getResourceAsStream(filePath)
                if (inputStream != null) {
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            line?.trim()?.let { word ->
                                if (word.length >= 3 && word.all { it.isLetter() }) {
                                    wordRepository.addWord(word)
                                    count++
                                }
                            }
                        }
                    }
                } else {
                    logger.error("Dictionary file not found: $filePath")
                }
            }

            logger.info("Dictionary initialized with $count words")
        } catch (e: Exception) {
            logger.error("Error initializing dictionary: ${e.message}")
        }

        return count
    }

    /**
     * Check if a word can be formed from the given letters
     */
    fun canFormWord(word: String, letters: String): Boolean {
        val letterCounts =
            letters.lowercase().groupBy { it }.mapValues { it.value.size }.toMutableMap()

        for (char in word.lowercase()) {
            val remainingCount = letterCounts[char] ?: 0
            if (remainingCount <= 0) return false
            letterCounts[char] = remainingCount - 1
        }

        return true
    }

    /**
     * Get valid words that can be formed from the given letters
     */
    suspend fun findPossibleWords(letters: String, minLength: Int = 3): List<Word> {
        // This would be an advanced function requiring efficient lookup
        // For now, a placeholder implementation:
        return emptyList()
    }
}