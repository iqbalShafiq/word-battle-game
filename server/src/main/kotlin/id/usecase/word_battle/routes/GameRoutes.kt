package id.usecase.word_battle.routes

import id.usecase.word_battle.network.game.CreateGameRequest
import id.usecase.word_battle.network.game.ValidateWordRequest
import id.usecase.word_battle.service.GameService
import id.usecase.word_battle.service.MatchmakingService
import id.usecase.word_battle.service.WordValidationService
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
 * Configure game-related REST endpoints
 */
fun Application.gameRoutes() {
    val gameService by inject<GameService>()
    val wordValidationService by inject<WordValidationService>()
    val matchmakingService by inject<MatchmakingService>()

    routing {
        authenticate("auth-jwt") {
            route("/api/games") {
                /**
                 * Create a new game with invited players
                 */
                post("/create") {
                    val principal = call.principal<JWTPrincipal>()
                    val playerId = principal?.getClaim("userId", String::class)
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Invalid token")
                        )

                    val request = call.receive<CreateGameRequest>()

                    // Ensure creator is included in player list
                    val allPlayers = (request.playerIds + playerId).distinct()

                    if (allPlayers.size < 2) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "At least 2 players are required")
                        )
                        return@post
                    }

                    val game = gameService.createGameSession(allPlayers, request.gameMode)

                    call.respond(
                        mapOf(
                            "message" to "Game created successfully",
                            "gameId" to game?.id
                        )
                    )
                }

                /**
                 * Get my active games
                 */
                get("/my-games") {
                    val principal = call.principal<JWTPrincipal>()
                    val playerId = principal?.getClaim("userId", String::class)
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf("error" to "Invalid token")
                        )

                    val games = gameService.getAllPlayersInGame(playerId)

                    call.respond(games)
                }

                /**
                 * Get game details
                 */
                get("/{id}") {
                    val gameId = call.parameters["id"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Game ID is required")
                        )

                    val game = gameService.getGame(gameId)

                    if (game == null) {
                        return@get call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "Game not found")
                        )
                    }

                    call.respond(game)
                }

                /**
                 * Get rounds for a game
                 */
                get("/{id}/rounds") {
                    val gameId = call.parameters["id"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Game ID is required")
                        )

                    val rounds = gameService.getGameRound(gameId)

                    call.respond(rounds)
                }
            }

            route("/api/words") {
                /**
                 * Validate a word
                 */
                post("/validate") {
                    val request = call.receive<ValidateWordRequest>()

                    val result =
                        wordValidationService.validateWord(request.word, request.availableLetters)

                    call.respond(
                        mapOf(
                            "word" to request.word,
                            "isValid" to result.isValid,
                            "error" to result.reason
                        )
                    )
                }
            }
        }
    }
}