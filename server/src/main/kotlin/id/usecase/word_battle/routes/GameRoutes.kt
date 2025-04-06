package id.usecase.word_battle.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Game-related REST endpoints
 */
fun Routing.gameRoutes() {
    authenticate("auth-jwt") {
        route("/games") {
            get {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Game routes will be implemented in Step 4")
                )
            }
        }
    }
}