package id.usecase.word_battle.plugins

import id.usecase.evaluasi.dotenv.DotenvConfig.dotenv
import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val driverClassName = "org.postgresql.Driver"
    val dbHost = dotenv["DATABASE_HOST"]
    val dbPort = dotenv["DATABASE_PORT"]
    val dbName = dotenv["DATABASE_NAME"]

//    val jdbcURL = "jdbc:postgresql://db:$dbPort/" // prod
    val jdbcURL = "jdbc:postgresql://$dbHost:$dbPort/" // local
    val user = dotenv["DATABASE_USER"]
    val password = dotenv["DATABASE_PASSWORD"]
    val database = Database.Companion.connect(
        url = jdbcURL,
        driver = driverClassName,
        user = user.toString(),
        password = password.toString()
    )

    transaction(database) {
        exec("CREATE SCHEMA IF NOT EXISTS $dbName")
    }

    log.info("Database initialized")
}