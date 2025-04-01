package id.usecase.word_battle.config

import id.usecase.word_battle.config.Scoring.BASE_SCORE
import id.usecase.word_battle.config.Scoring.LETTER_MULTIPLIER
import id.usecase.word_battle.config.Scoring.SPEED_BONUS_MULTIPLIER

object GameConfig {
    // Letter sets (examples)
    val LETTER_SETS = listOf(
        "AIUEOKTPNM",
        "BCDFGHKLJM",
        "NOPQRSTUAI",
        "VWXYZABCDE",
    )

    /**
     * Calculate the score for a valid word
     */
    fun calculateWordScore(word: String, isFirst: Boolean = false): Int {
        val baseScore = BASE_SCORE + (word.length * LETTER_MULTIPLIER)
        return if (isFirst) {
            (baseScore * (1 + SPEED_BONUS_MULTIPLIER)).toInt()
        } else {
            baseScore
        }
    }
}