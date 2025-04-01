package id.usecase.word_battle.data.models.word

import org.jetbrains.exposed.sql.Table

/**
 * Words table - Dictionary of valid Indonesian words
 */
object Words : Table() {
    val id = integer("id").autoIncrement()
    val word = varchar("word", 100).uniqueIndex()
    val isValid = bool("is_valid").default(true)
    val length = integer("length")

    override val primaryKey = PrimaryKey(id)
}