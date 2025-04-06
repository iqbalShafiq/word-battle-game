package id.usecase.word_battle.data.repository

import id.usecase.word_battle.data.DatabaseFactory.dbQuery
import id.usecase.word_battle.data.models.word.Word
import id.usecase.word_battle.data.models.word.WordsTable
import id.usecase.word_battle.game.LetterGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Random
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.charLength
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.io.BufferedReader

/**
 * Database implementation of WordRepository
 */
class WordRepositoryImpl : WordRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val letterGenerator = LetterGenerator()
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Initialize dictionary if needed
        scope.launch {
            initDictionary()
        }
    }

    /**
     * Initialize dictionary if empty
     */
    private suspend fun initDictionary() {
        // This will run in a non-suspended blocking context
        newSuspendedTransaction(Dispatchers.IO) {
            // Check if dictionary is empty
            val wordCount = WordsTable.selectAll().count()

            if (wordCount == 0L) {
                logger.info("Dictionary is empty, loading default words...")
                loadDefaultDictionary()
            } else {
                logger.info("Dictionary already contains $wordCount words")
            }
        }
    }

    /**
     * Load default dictionary from resources
     */
    private fun loadDefaultDictionary() {
        try {
            val resourceStream = javaClass
                .classLoader
                .getResourceAsStream("dictionary/english_words.txt")

            if (resourceStream == null) {
                logger.warn("Dictionary file not found, loading minimal set")
                loadMinimalWordSet()
                return
            }

            BufferedReader(resourceStream.reader()).use { reader ->
                var count = 0
                val batchSize = 500
                val wordBatch = mutableListOf<String>()

                reader.lineSequence().forEach { line ->
                    val word = line.trim().lowercase()
                    if (word.length >= 3 && word.all { it.isLetter() }) {
                        wordBatch.add(word)
                        count++

                        // Insert in batches for better performance
                        if (wordBatch.size >= batchSize) {
                            insertWordBatch(wordBatch)
                            wordBatch.clear()
                        }
                    }
                }

                // Insert any remaining words
                if (wordBatch.isNotEmpty()) {
                    insertWordBatch(wordBatch)
                }

                logger.info("Loaded $count words into dictionary")
            }
        } catch (e: Exception) {
            logger.error("Failed to load dictionary: ${e.message}")
            loadMinimalWordSet()
        }
    }

    /**
     * Insert a batch of words into the database
     */
    private fun insertWordBatch(words: List<String>) {
        WordsTable.batchInsert(words) { word ->
            this[WordsTable.word] = word
            this[WordsTable.isValid] = true
        }
    }

    /**
     * Load a minimal set of words as fallback
     */
    private fun loadMinimalWordSet() {
        val basicWords = listOf(
            "cat", "dog", "run", "jump", "play", "happy", "sad",
            "big", "small", "red", "blue", "green", "yellow",
            "apple", "banana", "orange", "grape", "melon",
            "computer", "phone", "tablet", "laptop", "screen",
            "house", "building", "door", "window", "room",
            "car", "bus", "train", "plane", "boat",
            "pizza", "pasta", "bread", "cake", "cookie",
            "water", "juice", "milk", "tea", "coffee"
        )

        insertWordBatch(basicWords)
        logger.info("Loaded ${basicWords.size} basic words as fallback")
    }

    /**
     * Check if a word is valid according to the dictionary
     */
    override suspend fun isValidWord(word: String): Boolean = dbQuery {
        val cleanWord = word.trim().lowercase()

        WordsTable
            .selectAll()
            .where { (WordsTable.word eq cleanWord) and (WordsTable.isValid eq true) }
            .count() > 0
    }

    /**
     * Add a word to the dictionary
     */
    override suspend fun addWord(word: String, isValid: Boolean): Word? = dbQuery {
        val cleanWord = word.trim().lowercase()

        if (cleanWord.isBlank() || !cleanWord.all { it.isLetter() }) {
            logger.warn("Cannot add invalid word: '$word'")
            return@dbQuery null
        }

        // Check if word already exists
        val existingWord = WordsTable
            .selectAll()
            .where { WordsTable.word eq cleanWord }
            .singleOrNull()

        if (existingWord != null) {
            // Return existing word
            return@dbQuery Word(
                id = existingWord[WordsTable.id],
                word = existingWord[WordsTable.word],
                isValid = existingWord[WordsTable.isValid]
            )
        }

        val wordId = WordsTable.insert {
            it[WordsTable.word] = cleanWord
            it[WordsTable.isValid] = isValid
        } get WordsTable.id

        logger.info("Added word '$cleanWord' to dictionary")

        Word(
            id = wordId,
            word = cleanWord,
            isValid = isValid
        )
    }

    /**
     * Get random word set with specified length
     */
    override suspend fun getRandomWordSet(length: Int, count: Int): List<Word> = dbQuery {
        WordsTable
            .selectAll()
            .where { (WordsTable.isValid eq true) and (WordsTable.word.charLength() eq length) }
            .orderBy(Random())
            .limit(count.coerceAtMost(100))
            .map { row ->
                Word(
                    id = row[WordsTable.id],
                    word = row[WordsTable.word],
                    isValid = row[WordsTable.isValid]
                )
            }
    }

    /**
     * Generate random letters for a game round
     */
    override suspend fun generateRandomLetters(length: Int): String {
        return letterGenerator.generateLetters(length.coerceIn(7, 10))
    }

    /**
     * Get a list of random words from the dictionary
     */
    override suspend fun getRandomWords(count: Int): List<String> = dbQuery {
        WordsTable
            .selectAll()
            .where { WordsTable.isValid eq true }
            .orderBy(Random())
            .limit(count.coerceAtMost(1000))
            .map { it[WordsTable.word] }
    }
}