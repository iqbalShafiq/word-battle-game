package id.usecase.word_battle.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import id.usecase.evaluasi.dotenv.DotenvConfig.dotenv
import id.usecase.word_battle.data.models.player.UserAccount
import java.util.Date

/**
 * JWT configuration for authentication
 */
object JwtConfig {
    private val SECRET = dotenv["JWT_SECRET"] ?: "word-battle-secure-secret"
    private const val ISSUER = "word-battle-server"
    private const val VALIDITY_IN_MS = 36_000_00 * 24 // 24 hours

    private val algorithm = Algorithm.HMAC512(SECRET)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .build()

    /**
     * Generate JWT token for a player
     */
    fun makeToken(gamePlayer: UserAccount): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(ISSUER)
        .withClaim("id", gamePlayer.id)
        .withClaim("username", gamePlayer.username)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    /**
     * Calculate token expiration date
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + VALIDITY_IN_MS)
}