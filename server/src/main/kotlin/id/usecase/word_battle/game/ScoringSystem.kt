package id.usecase.word_battle.game

import org.slf4j.LoggerFactory

/**
 * Scoring system for word battle game
 */
class ScoringSystem {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // Base scores for letters
    private val letterScores = mapOf(
        'a' to 1, 'b' to 3, 'c' to 3, 'd' to 2, 'e' to 1,
        'f' to 4, 'g' to 2, 'h' to 4, 'i' to 1, 'j' to 8,
        'k' to 5, 'l' to 1, 'm' to 3, 'n' to 1, 'o' to 1,
        'p' to 3, 'q' to 10, 'r' to 1, 's' to 1, 't' to 1,
        'u' to 1, 'v' to 4, 'w' to 4, 'x' to 8, 'y' to 4,
        'z' to 10
    )

    // Bonus calculation parameters
    private val lengthBonusThresholds = mapOf(
        4 to 1,  // 4 letters = +1
        5 to 2,  // 5 letters = +2
        6 to 3,  // 6 letters = +3
        7 to 5,  // 7 letters = +5
        8 to 8,  // 8 letters = +8
        9 to 11, // 9 letters = +11
        10 to 15 // 10+ letters = +15
    )

    // Special letter bonuses
    private val specialLetters = setOf('q', 'z', 'x', 'j')

    /**
     * Calculate score for a word
     */
    fun calculateScore(word: String): Int {
        // Clean the word
        val cleanWord = word.trim().lowercase()

        // Calculate base score from letter values
        val baseScore = cleanWord.sumOf { letterScores[it] ?: 0 }

        // Calculate length bonus
        val lengthBonus = calculateLengthBonus(cleanWord.length)

        // Special letter bonus (using rare letters)
        val specialBonus = if (cleanWord.any { it in specialLetters }) 5 else 0

        // Calculate total score
        val totalScore = baseScore + lengthBonus + specialBonus

        logger.debug("Score for '$cleanWord': $baseScore (base) + $lengthBonus (length) + $specialBonus (special) = $totalScore")

        return totalScore
    }

    /**
     * Calculate time bonus based on how quickly the word was submitted
     */
    fun calculateTimeBonus(elapsedSeconds: Int, roundDurationSeconds: Int): Int {
        // Only give time bonus if submitted in first half of the round
        if (elapsedSeconds > roundDurationSeconds / 2) {
            return 0
        }

        // The earlier the submission, the higher the bonus
        val timeRatio = 1.0 - (elapsedSeconds.toDouble() / roundDurationSeconds)
        return (timeRatio * 10).toInt()
    }

    /**
     * Calculate bonus points based on word length
     */
    private fun calculateLengthBonus(length: Int): Int {
        return lengthBonusThresholds.entries
            .filter { length >= it.key }
            .maxOfOrNull { it.value } ?: 0
    }

    /**
     * Calculate streak bonus when player submits multiple valid words in a row
     */
    fun calculateStreakBonus(streak: Int): Int {
        return when {
            streak >= 5 -> 10
            streak >= 3 -> 5
            else -> 0
        }
    }

    /**
     * Calculate penalty for incorrect words
     * (optional, can be used to discourage random guessing)
     */
    fun calculatePenalty(): Int {
        return -2
    }
}