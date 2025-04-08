package id.usecase.word_battle.plugins

import id.usecase.word_battle.Greeting
import id.usecase.word_battle.routes.authRoutes
import id.usecase.word_battle.routes.gameRoutes
import id.usecase.word_battle.routes.socketRoutes
import id.usecase.word_battle.routes.statusRoutes
import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") { call.respondText("Ktor: ${Greeting().greet()}") }
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        statusRoutes()
    }
    authRoutes()
    gameRoutes()
    socketRoutes()
}