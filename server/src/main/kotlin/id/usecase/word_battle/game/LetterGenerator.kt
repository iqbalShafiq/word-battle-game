package id.usecase.word_battle.game

import kotlin.random.Random

/**
 * Generates random letters for game rounds with proper vowel/consonant distribution
 */
class LetterGenerator {
    private val vowels = listOf('a', 'e', 'i', 'o', 'u')

    private val consonants = listOf(
        'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm',
        'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'
    )

    // Letter frequency weights for English language
    private val consonantWeights = mapOf(
        'b' to 2, 'c' to 3, 'd' to 4, 'f' to 2, 'g' to 2,
        'h' to 2, 'j' to 1, 'k' to 1, 'l' to 4, 'm' to 3,
        'n' to 6, 'p' to 2, 'q' to 1, 'r' to 6, 's' to 6,
        't' to 9, 'v' to 1, 'w' to 2, 'x' to 1, 'y' to 2, 'z' to 1
    )

    private val vowelWeights = mapOf(
        'a' to 8, 'e' to 12, 'i' to 7, 'o' to 8, 'u' to 3
    )

    /**
     * Generate random letters with a good mix of vowels and consonants
     */
    fun generateLetters(count: Int): String {
        // Ensure at least 2 vowels if count >= 6, otherwise at least 1
        val minVowels = if (count >= 6) 2 else 1

        // Calculate optimal vowel count
        val maxVowels = (count * 0.4).toInt().coerceIn(minVowels, count - minVowels)
        val vowelCount = Random.nextInt(minVowels, maxVowels + 1)
        val consonantCount = count - vowelCount

        // Generate vowels and consonants
        val selectedVowels = selectRandomLetters(vowels, vowelCount, vowelWeights)
        val selectedConsonants = selectRandomLetters(consonants, consonantCount, consonantWeights)

        // Combine and shuffle
        return (selectedVowels + selectedConsonants).shuffled().joinToString("")
    }

    /**
     * Select random letters with weights
     */
    private fun selectRandomLetters(
        source: List<Char>,
        count: Int,
        weights: Map<Char, Int>
    ): List<Char> {
        val selected = mutableListOf<Char>()

        while (selected.size < count) {
            // Choose letter based on weights
            val letter = weightedRandomChoice(source, weights)

            // Limit repetition to avoid too many duplicates
            if (selected.count { it == letter } < 2) {
                selected.add(letter)
            } else if (Random.nextDouble() < 0.2) { // 20% chance to allow a third repeat
                selected.add(letter)
            }
        }

        return selected
    }

    /**
     * Choose a random item from a list based on weights
     */
    private fun weightedRandomChoice(items: List<Char>, weights: Map<Char, Int>): Char {
        val totalWeight = items.sumOf { weights[it] ?: 1 }
        var randomValue = Random.nextInt(totalWeight)

        for (item in items) {
            val weight = weights[item] ?: 1
            if (randomValue < weight) return item
            randomValue -= weight
        }

        // Should never get here, but return first item as fallback
        return items.first()
    }
}