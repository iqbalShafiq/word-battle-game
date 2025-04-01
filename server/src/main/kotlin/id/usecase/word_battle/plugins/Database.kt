package id.usecase.word_battle.plugins

import id.usecase.word_battle.data.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.log

/**
 * Configure database connection and initialization
 */
fun Application.configureDatabase() {
    try {
        DatabaseFactory.init()
    } catch (e: Exception) {
        log.error("Failed to configure database: ${e.message}")
    }
}