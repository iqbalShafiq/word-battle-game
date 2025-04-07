package id.usecase.word_battle.routes

import id.usecase.word_battle.network.auth.AuthRequest
import id.usecase.word_battle.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

/**
 * Configure authentication routes
 */
fun Application.authRoutes() {
    val userService: UserService by inject()

    routing {
        route("/auth") {
            post("/register") {
                val request = call.receive<AuthRequest>()
                val result = userService.registerUser(request)

                call.respond(
                    if (result.success) HttpStatusCode.Created else HttpStatusCode.BadRequest,
                    result
                )
            }

            post("/login") {
                val request = call.receive<AuthRequest>()
                val result = userService.loginUser(request)

                call.respond(
                    if (result.success) HttpStatusCode.OK else HttpStatusCode.Unauthorized,
                    result
                )
            }

            // Route protected by JWT authentication
            authenticate("auth-jwt") {
                get("/profile") {
                    val principal = call.principal<JWTPrincipal>()
                    val playerId = principal?.payload?.getClaim("id")?.asString()

                    if (playerId != null) {
                        val profile = userService.getPlayerProfile(playerId)
                        if (profile != null) {
                            call.respond(profile)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Player not found")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                    }
                }
            }
        }
    }
}