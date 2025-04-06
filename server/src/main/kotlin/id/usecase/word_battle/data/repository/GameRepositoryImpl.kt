package id.usecase.word_battle.data.repository

import id.usecase.word_battle.data.DatabaseFactory.dbQuery
import id.usecase.word_battle.data.models.game.GameRoundEntity
import id.usecase.word_battle.data.models.game.GameRoundsTable
import id.usecase.word_battle.data.models.game.GameSession
import id.usecase.word_battle.data.models.game.GameSessionsTable
import id.usecase.word_battle.data.models.game.WordSubmissionEntity
import id.usecase.word_battle.models.GameMode
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID

/**
 * Database implementation of GameRepository
 */
class GameRepositoryImpl : GameRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Create a new game session
     */
    override suspend fun createGameSession(
        playerIds: List<String>,
        gameMode: GameMode
    ): GameSession? =
        dbQuery {
            if (playerIds.size < 2) {
                logger.warn("Cannot create game with less than 2 players")
                return@dbQuery null
            }

            val timestamp = Instant.now()

            // Insert game session into database
            val gameId = GameSessionsTable.insert {
                it[createdAt] = timestamp
                it[endedAt] = null
                it[players] = json.encodeToString(playerIds)
                it[this.gameMode] = gameMode.name
                it[winnerId] = null
                it[isActive] = true
            } get GameSessionsTable.id

            logger.info("Created game session $gameId with ${playerIds.size} players")

            GameSession(
                id = gameId.value.toString(),
                createdAt = timestamp.epochSecond,
                players = playerIds,
                gameMode = gameMode,
                isActive = true
            )
        }

    /**
     * Get game session by ID
     */
    override suspend fun getGameSession(id: String): GameSession? = dbQuery {
        GameSessionsTable.selectAll()
            .where { GameSessionsTable.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.let { row ->
                GameSession(
                    id = row[GameSessionsTable.id].value.toString(),
                    createdAt = row[GameSessionsTable.createdAt].epochSecond,
                    endedAt = row[GameSessionsTable.endedAt]?.epochSecond,
                    players = json.decodeFromString(row[GameSessionsTable.players]),
                    winnerId = row[GameSessionsTable.winnerId]?.value.toString(),
                    gameMode = GameMode.valueOf(row[GameSessionsTable.gameMode]),
                    isActive = row[GameSessionsTable.isActive]
                )
            }
    }

    /**
     * Get active game sessions
     */
    override suspend fun getActiveGameSessions(limit: Int, offset: Int): List<GameSession> =
        dbQuery {
            GameSessionsTable.selectAll().where { GameSessionsTable.isActive eq true }
                .orderBy(GameSessionsTable.createdAt, SortOrder.DESC)
                .limit(limit, offset.toLong())
                .map { row ->
                    GameSession(
                        id = row[GameSessionsTable.id].value.toString(),
                        createdAt = row[GameSessionsTable.createdAt].epochSecond,
                        endedAt = row[GameSessionsTable.endedAt]?.epochSecond,
                        players = json.decodeFromString(row[GameSessionsTable.players]),
                        winnerId = row[GameSessionsTable.winnerId]?.value.toString(),
                        gameMode = GameMode.valueOf(row[GameSessionsTable.gameMode]),
                        isActive = row[GameSessionsTable.isActive]
                    )
                }
        }

    /**
     * End game session and set winner
     */
    override suspend fun endGameSession(id: String, winnerId: String?): Boolean =
        dbQuery {
            val updatedRows = GameSessionsTable
                .update({ GameSessionsTable.id eq UUID.fromString(id) }) {
                    it[isActive] = false
                    it[endedAt] = Instant.now()
                    it[GameSessionsTable.winnerId] = winnerId?.let { UUID.fromString(it) }
                }

            if (updatedRows > 0) {
                logger.info("Ended game session $id with winner ${winnerId ?: "none"}")
                true
            } else {
                logger.warn("Failed to end game session $id: session not found")
                false
            }
        }

    /**
     * Create a new game round
     */
    override suspend fun createGameRound(
        gameSessionId: String,
        roundNumber: Int,
        letters: String
    ): GameRoundEntity? =
        dbQuery {
            // Check if game exists and is active
            val gameExists = GameSessionsTable
                .selectAll()
                .where { (GameSessionsTable.id eq UUID.fromString(gameSessionId)) and (GameSessionsTable.isActive eq true) }
                .count() > 0

            if (!gameExists) {
                logger.warn("Cannot create round for non-existent or inactive game $gameSessionId")
                return@dbQuery null
            }

            // Insert round into database
            val roundId = GameRoundsTable.insert {
                it[this.gameSessionId] = UUID.fromString(gameSessionId)
                it[this.roundNumber] = roundNumber
                it[this.letters] = letters
                it[submissions] = "[]" // Empty JSON array
            } get GameRoundsTable.id

            logger.info("Created round $roundId for game $gameSessionId")

            GameRoundEntity(
                id = roundId.value.toString(),
                gameSessionId = gameSessionId,
                roundNumber = roundNumber,
                letters = letters,
                submissions = mutableListOf()
            )
        }

    /**
     * Add word submission to a round
     */
    override suspend fun addWordSubmission(
        roundId: String,
        submission: WordSubmissionEntity
    ): Boolean =
        dbQuery {
            // Get current round data
            val roundData = GameRoundsTable
                .selectAll()
                .where { GameRoundsTable.id eq UUID.fromString(roundId) }
                .singleOrNull() ?: return@dbQuery false

            // Get current submissions
            val currentSubmissions: MutableList<WordSubmissionEntity> =
                if (roundData[GameRoundsTable.submissions] == "[]") {
                    mutableListOf()
                } else {
                    json.decodeFromString(roundData[GameRoundsTable.submissions])
                }

            // Check for duplicate submission
            val isDuplicate = currentSubmissions.any {
                it.playerId == submission.playerId && it.word.equals(
                    submission.word,
                    ignoreCase = true
                )
            }

            if (isDuplicate) {
                logger.info("Player ${submission.playerId} tried to submit duplicate word: ${submission.word}")
                return@dbQuery false
            }

            // Create a copy of the submission with a timestamp if not already set
            val timestampedSubmission = submission.copy(timestamp = Instant.now().epochSecond)

            // Add new submission
            val updatedSubmissions = currentSubmissions + timestampedSubmission

            // Update database
            val updatedRows = GameRoundsTable
                .update({ GameRoundsTable.id eq UUID.fromString(roundId) }) {
                    it[submissions] = json.encodeToString(updatedSubmissions)
                }

            if (updatedRows > 0) {
                logger.info("Added submission for word '${submission.word}' by player ${submission.playerId} to round $roundId")
                true
            } else {
                logger.warn("Failed to add submission to round $roundId")
                false
            }
        }

    /**
     * Get rounds for a game session
     */
    override suspend fun getRoundsForGameSession(gameSessionId: String): List<GameRoundEntity> =
        dbQuery {
            GameRoundsTable
                .selectAll()
                .where { GameRoundsTable.gameSessionId eq UUID.fromString(gameSessionId) }
                .orderBy(GameRoundsTable.roundNumber)
                .map { row ->
                    GameRoundEntity(
                        id = row[GameRoundsTable.id].value.toString(),
                        gameSessionId = row[GameRoundsTable.gameSessionId].value.toString(),
                        roundNumber = row[GameRoundsTable.roundNumber],
                        letters = row[GameRoundsTable.letters],
                        submissions = json.decodeFromString(row[GameRoundsTable.submissions])
                    )
                }
        }

    /**
     * Get a specific round by ID
     */
    override suspend fun getRound(roundId: String): GameRoundEntity? =
        dbQuery {
            GameRoundsTable
                .selectAll()
                .where { GameRoundsTable.id eq UUID.fromString(roundId) }
                .singleOrNull()
                ?.let { row ->
                    GameRoundEntity(
                        id = row[GameRoundsTable.id].value.toString(),
                        gameSessionId = row[GameRoundsTable.gameSessionId].value.toString(),
                        roundNumber = row[GameRoundsTable.roundNumber],
                        letters = row[GameRoundsTable.letters],
                        submissions = json.decodeFromString(row[GameRoundsTable.submissions])
                    )
                }
        }

    /**
     * Get active games for a player
     */
    override suspend fun getActiveGamesForPlayer(playerId: String): List<GameSession> =
        dbQuery {
            val playerJson = "\"%$playerId%\"" // Pattern to search for playerId in JSON array

            // Use SQL LIKE to search for player ID in JSON array string (basic approach)
            // A better approach would use JSON functions if available in your SQL dialect
            GameSessionsTable
                .selectAll()
                .where { (GameSessionsTable.isActive eq true) and (GameSessionsTable.players like playerJson) }
                .orderBy(GameSessionsTable.createdAt, SortOrder.DESC)
                .map { row ->
                    val players: List<String> = json.decodeFromString(
                        row[GameSessionsTable.players]
                    )

                    // Double-check player is actually in the list (to avoid false positives with LIKE)
                    if (playerId !in players) {
                        return@map null
                    }

                    GameSession(
                        id = row[GameSessionsTable.id].value.toString(),
                        createdAt = row[GameSessionsTable.createdAt].epochSecond,
                        endedAt = row[GameSessionsTable.endedAt]?.epochSecond,
                        players = players,
                        winnerId = row[GameSessionsTable.winnerId]?.value.toString(),
                        gameMode = GameMode.valueOf(row[GameSessionsTable.gameMode]),
                        isActive = row[GameSessionsTable.isActive]
                    )
                }
                .filterNotNull()
        }
}