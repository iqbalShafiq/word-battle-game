package id.usecase.word_battle.data.models.game

import org.jetbrains.exposed.dao.id.UUIDTable

/**
 * GameRounds table - Stores information about individual rounds within a game
 */
object GameRoundsTable : UUIDTable() {
    val gameSessionId = reference("game_session_id", GameSessionsTable)
    val roundNumber = integer("round_number")
    val letters = varchar("letters", 20)
    val submissions = text("submissions").default("[]")
    val roundStats = text("round_stats").default("{}")

    init {
        index(isUnique = false, gameSessionId)
    }
}