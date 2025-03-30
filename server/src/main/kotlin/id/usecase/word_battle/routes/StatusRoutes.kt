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
                mapOf(
                    "status" to "OK",
                    "timestamp" to Instant.now().toString(),
                    "version" to "0.0.1"
                )
            )
        }
    }

    // Basic health check endpoint for load balancers
    get("/health") {
        call.respond(HttpStatusCode.OK)
    }
}