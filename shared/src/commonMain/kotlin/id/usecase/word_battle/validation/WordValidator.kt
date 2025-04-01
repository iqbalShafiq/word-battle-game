package id.usecase.word_battle.validation

/**
 * Validates if a word can be formed from given letters
 */
class WordValidator {
    companion object {
        /**
         * Checks if a word can be formed from the given set of letters
         */
        fun canFormWord(word: String, letters: String): Boolean {
            // Convert to lowercase for case-insensitive comparison
            val wordLower = word.lowercase()
            val lettersLower = letters.lowercase()

            // Count frequency of each letter in the available letters
            val letterCounts = mutableMapOf<Char, Int>()
            for (c in lettersLower) {
                letterCounts[c] = letterCounts.getOrDefault(c, 0) + 1
            }

            // Check if word can be formed from available letters
            for (c in wordLower) {
                val count = letterCounts.getOrDefault(c, 0)
                if (count <= 0) return false
                letterCounts[c] = count - 1
            }

            return true
        }
    }
}