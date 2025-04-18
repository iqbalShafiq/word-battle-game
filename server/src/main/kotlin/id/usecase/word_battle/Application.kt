@file:Suppress("unused")

package id.usecase.word_battle

import id.usecase.word_battle.plugins.configureCors
import id.usecase.word_battle.plugins.configureDatabase
import id.usecase.word_battle.plugins.configureKoin
import id.usecase.word_battle.plugins.configureLifecycle
import id.usecase.word_battle.plugins.configureMonitoring
import id.usecase.word_battle.plugins.configureRouting
import id.usecase.word_battle.plugins.configureSecurity
import id.usecase.word_battle.plugins.configureSerialization
import id.usecase.word_battle.plugins.configureSockets
import io.ktor.server.application.Application
import io.ktor.server.application.log

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureKoin()
    configureDatabase()
    configureCors()
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureSockets()
    configureRouting()
    configureLifecycle()

    log.info("Word Battle server started")
}