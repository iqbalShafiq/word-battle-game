package id.usecase.word_battle.plugins

import id.usecase.word_battle.security.JwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

/**
 * Configure security features like JWT authentication
 */
fun Application.configureSecurity() {
    authentication {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                val id = credential.payload.getClaim("id").asString()
                val username = credential.payload.getClaim("username").asString()
                if (id.isNotEmpty() && username.isNotEmpty()) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is invalid or has expired")
            }
        }
    }

    log.info("Security features configured")
}