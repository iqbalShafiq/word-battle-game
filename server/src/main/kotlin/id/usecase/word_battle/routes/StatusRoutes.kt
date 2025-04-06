package id.usecase.word_battle.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.Instant

fun Routing.statusRoutes() {
    route("/status") {
        get {
            call.respond(
                HttpStatusCode.OK,
                mapOf("status" to "running")
            )
        }

        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "status" to "healthy",
                    "version" to "1.0.0",
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }
}