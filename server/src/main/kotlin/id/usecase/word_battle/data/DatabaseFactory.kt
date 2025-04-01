package id.usecase.word_battle.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import id.usecase.evaluasi.dotenv.DotenvConfig.dotenv
import id.usecase.word_battle.data.models.game.GameRounds
import id.usecase.word_battle.data.models.game.GameSessions
import id.usecase.word_battle.data.models.player.Players
import id.usecase.word_battle.data.models.word.Words
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Initialize database with connection pool and run migrations
     */
    fun init() {
        val dbHost = dotenv["DATABASE_HOST"]
        val dbPort = dotenv["DATABASE_PORT"]
        val dbName = dotenv["DATABASE_NAME"]

//    val jdbcUrl = "jdbc:postgresql://db:$dbPort/" // prod
        val jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName" // local
        logger.info("Initializing database: $jdbcUrl")

        val dbUser = dotenv["DATABASE_USER"]
        val dbPassword = dotenv["DATABASE_PASSWORD"]
        val dataSource = hikari(jdbcUrl, dbUser, dbPassword)

        // Run migrations
        runFlyway(dataSource)

        // Connect Exposed to the database
        val database = Database.connect(dataSource)

        // Create tables if they don't exist (redundant with Flyway but useful for verification)
        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(Players, GameSessions, GameRounds, Words)
        }

        logger.info("Database initialized successfully")
    }

    /**
     * Create HikariCP data source for efficient connection pooling
     */
    private fun hikari(jdbcUrl: String, dbUser: String, dbPassword: String): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            this.jdbcUrl = jdbcUrl
            username = dbUser
            password = dbPassword
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    /**
     * Run Flyway migrations
     */
    private fun runFlyway(dataSource: DataSource) {
        logger.info("Running Flyway migrations...")

        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("db/migration")
            .load()

        val migrationResult = flyway.migrate()
        logger.info("Applied ${migrationResult.migrationsExecuted} migrations")
    }

    /**
     * Helper function for executing database queries with coroutine support
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            block()
        }
}