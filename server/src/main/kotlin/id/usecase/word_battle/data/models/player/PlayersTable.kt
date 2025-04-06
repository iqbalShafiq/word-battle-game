package id.usecase.word_battle.data.models.player

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Players table - Stores user account information
 */
object PlayersTable : UUIDTable() {
    val username = varchar("username", 255).uniqueIndex()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val lastActive = timestamp("last_active").defaultExpression(CurrentTimestamp)
    val stats = text("stats").default("{}")
}