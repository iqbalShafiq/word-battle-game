package id.usecase.word_battle.data.models.game

import id.usecase.word_battle.data.models.player.PlayersTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * GameSessions table - Stores active and completed game information
 */
object GameSessionsTable : UUIDTable() {
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val endedAt = timestamp("ended_at").nullable()
    val players = text("players").default("[]")
    val winnerId = reference("winner_id", PlayersTable.id).nullable()
    val gameMode = varchar("game_mode", 50).default("CLASSIC")
    val isActive = bool("is_active").default(true)
}