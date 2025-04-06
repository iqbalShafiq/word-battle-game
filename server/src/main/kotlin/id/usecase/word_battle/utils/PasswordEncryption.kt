package id.usecase.word_battle.utils

import java.security.MessageDigest
import java.util.Base64

/**
 * Utility for password encryption
 * In production, use a proper password hashing library like BCrypt
 */
object PasswordEncryption {
    /**
     * Hash a password using SHA-256
     * @param password Plain text password
     * @return Hashed password
     */
    fun hash(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.getEncoder().encodeToString(digest)
    }

    /**
     * Check if plain text password matches hashed password
     * @param plainPassword Plain text password to check
     * @param hashedPassword Stored hashed password to compare against
     * @return True if passwords match
     */
    fun verify(plainPassword: String, hashedPassword: String): Boolean {
        return hash(plainPassword) == hashedPassword
    }
}