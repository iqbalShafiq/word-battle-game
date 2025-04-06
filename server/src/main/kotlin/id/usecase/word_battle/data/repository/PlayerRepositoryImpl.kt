package id.usecase.word_battle.data.repository

import id.usecase.word_battle.data.DatabaseFactory.dbQuery
import id.usecase.word_battle.data.models.player.PlayerStats
import id.usecase.word_battle.data.models.player.PlayersTable
import id.usecase.word_battle.data.models.player.UserAccount
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID

/**
 * Database implementation of PlayerRepository
 */
class PlayerRepositoryImpl : PlayerRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Create a new player
     */
    override suspend fun createPlayer(username: String): UserAccount? = dbQuery {
        // Check for duplicate username
        val existingUser = PlayersTable
            .selectAll()
            .where { PlayersTable.username.lowerCase() eq username.lowercase() }
            .singleOrNull()

        if (existingUser != null) {
            logger.warn("Cannot create player: username '$username' already exists")
            return@dbQuery null
        }

        val timestamp = Instant.now()
        val emptyStats = PlayerStats()

        val playerId = PlayersTable.insert {
            it[this.username] = username
            it[createdAt] = timestamp
            it[lastActive] = timestamp
            it[stats] = json.encodeToString(emptyStats)
        } get PlayersTable.id

        logger.info("Created player $playerId with username '$username'")

        UserAccount(
            id = playerId.value.toString(),
            username = username,
            createdAt = timestamp.epochSecond,
            lastActive = timestamp.epochSecond,
            stats = emptyStats
        )
    }

    /**
     * Get player by ID
     */
    override suspend fun getPlayer(id: String): UserAccount? = dbQuery {
        PlayersTable.selectAll()
            .where { PlayersTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.let { row ->
                UserAccount(
                    id = row[PlayersTable.id].value.toString(),
                    username = row[PlayersTable.username],
                    createdAt = row[PlayersTable.createdAt].epochSecond,
                    lastActive = row[PlayersTable.lastActive].epochSecond,
                    stats = json.decodeFromString(row[PlayersTable.stats])
                )
            }
    }

    /**
     * Get player by username
     */
    override suspend fun getPlayerByUsername(username: String): UserAccount? = dbQuery {
        PlayersTable.selectAll()
            .where { PlayersTable.username.lowerCase() eq username.lowercase() }
            .singleOrNull()
            ?.let { row ->
                UserAccount(
                    id = row[PlayersTable.id].value.toString(),
                    username = row[PlayersTable.username],
                    createdAt = row[PlayersTable.createdAt].epochSecond,
                    lastActive = row[PlayersTable.lastActive].epochSecond,
                    stats = json.decodeFromString(row[PlayersTable.stats])
                )
            }
    }

    /**
     * Update player's last active timestamp
     */
    override suspend fun updateLastActive(id: String): Boolean = dbQuery {
        val currentTime = Instant.now()

        val updatedRows = PlayersTable
            .update({ PlayersTable.id eq UUID.fromString(id) }) {
                it[lastActive] = currentTime
            }

        updatedRows > 0
    }

    /**
     * Update player's statistics
     */
    override suspend fun updateStats(
        id: String,
        gamesPlayed: Int,
        gamesWon: Int,
        totalScore: Int,
        wordsFound: Int
    ): Boolean = dbQuery {
        // Get current stats
        val playerRow = PlayersTable
            .selectAll()
            .where { PlayersTable.id eq UUID.fromString(id) }
            .singleOrNull() ?: return@dbQuery false

        val currentStats: PlayerStats = json.decodeFromString(playerRow[PlayersTable.stats])

        // Update stats
        val updatedStats = currentStats.copy(
            gamesPlayed = currentStats.gamesPlayed + gamesPlayed,
            gamesWon = currentStats.gamesWon + gamesWon,
            totalScore = currentStats.totalScore + totalScore,
            wordsFound = currentStats.wordsFound + wordsFound
        )

        // Update database
        val updatedRows = PlayersTable
            .update({ PlayersTable.id eq UUID.fromString(id) }) {
                it[stats] = json.encodeToString(updatedStats)
            }

        if (updatedRows > 0) {
            logger.info("Updated stats for player $id: +$gamesPlayed games, +$gamesWon wins, +$totalScore score, +$wordsFound words")
            true
        } else {
            false
        }
    }
}