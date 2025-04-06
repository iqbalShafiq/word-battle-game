package id.usecase.word_battle.routes

import id.usecase.word_battle.security.JwtConfig
import id.usecase.word_battle.websocket.WebSocketController
import io.ktor.server.routing.Routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Routing.socketRoutes() {
    val controller by inject<WebSocketController>()
    val logger = LoggerFactory.getLogger(Routing::class.java)

    webSocket("/game") {
        try {
            // Extract token from query parameters
            val token = call.request.queryParameters["token"]
            if (token.isNullOrEmpty()) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Token required"))
                return@webSocket
            }

            try {
                // Verify token
                val decodedJWT = JwtConfig.verifier.verify(token)

                // Extract player ID
                val playerId = decodedJWT.getClaim("id").asString()
                if (playerId.isNullOrEmpty()) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
                    return@webSocket
                }

                // Handle the WebSocket connection
                controller.handleConnection(this, playerId)
            } catch (e: Exception) {
                logger.error("Invalid token: ${e.message}")
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
            }
        } catch (_: ClosedReceiveChannelException) {
            logger.info("WebSocket connection closed")
        } catch (e: Exception) {
            logger.error("WebSocket error: ${e.message}")
            close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, e.message ?: "Internal error"))
        }
    }
}