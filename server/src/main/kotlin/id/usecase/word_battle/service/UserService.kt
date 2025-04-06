package id.usecase.word_battle.service

import id.usecase.word_battle.data.models.player.UserAccount
import id.usecase.word_battle.data.repository.PlayerRepository
import id.usecase.word_battle.models.UserProfile
import id.usecase.word_battle.models.UserStats
import id.usecase.word_battle.network.auth.AuthRequest
import id.usecase.word_battle.network.auth.AuthResponse
import id.usecase.word_battle.security.JwtConfig
import org.slf4j.LoggerFactory

/**
 * Service for user-related operations
 */
class UserService(private val playerRepository: PlayerRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Register a new user
     */
    suspend fun registerUser(request: AuthRequest): AuthResponse {
        // Check if username exists
        val existingUser = playerRepository.getPlayerByUsername(request.username)
        if (existingUser != null) {
            logger.info("Registration failed: Username '${request.username}' already exists")
            return AuthResponse(
                success = false,
                message = "Username already exists",
                token = null,
                user = null
            )
        }

        // Create a new player
        val player = playerRepository.createPlayer(request.username)
        if (player == null) {
            logger.error("Failed to create user: ${request.username}")
            return AuthResponse(
                success = false,
                message = "Failed to create user",
                token = null,
                user = null
            )
        }

        logger.info("User registered successfully: ${request.username}")
        // Generate JWT token
        val token = JwtConfig.makeToken(player)

        return AuthResponse(
            success = true,
            message = "User registered successfully",
            token = token,
            user = player.toUserProfile()
        )
    }

    /**
     * Login a user
     */
    suspend fun loginUser(request: AuthRequest): AuthResponse {
        val player = playerRepository.getPlayerByUsername(request.username)
        if (player == null) {
            logger.info("Login failed: Username '${request.username}' not found")
            return AuthResponse(
                success = false,
                message = "Invalid username or password",
                token = null,
                user = null
            )
        }

        // Update last active time
        playerRepository.updateLastActive(player.id)

        // Generate JWT token
        val token = JwtConfig.makeToken(player)

        logger.info("User logged in successfully: ${request.username}")
        return AuthResponse(
            success = true,
            message = "Login successful",
            token = token,
            user = player.toUserProfile()
        )
    }

    fun UserAccount.toUserProfile() = UserProfile(
        id = this.id,
        username = this.username,
        stats = UserStats(
            gamesPlayed = this.stats.gamesPlayed,
            gamesWon = this.stats.gamesWon,
            totalScore = this.stats.totalScore
        )
    )

    /**
     * Get player profile by ID
     */
    suspend fun getPlayerProfile(id: String): UserAccount? {
        return playerRepository.getPlayer(id)
    }
}