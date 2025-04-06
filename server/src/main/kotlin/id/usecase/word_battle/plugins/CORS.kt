package id.usecase.word_battle.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.cors.routing.CORS

/**
 * Configure Cross-Origin Resource Sharing (CORS)
 */
fun Application.configureCors() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // Allow all headers
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)

        // Allow requests from any origin in development
        // In production, this should be restricted
        anyHost()

        // Allow credentials like cookies
        allowCredentials = true

        // How long the browser should cache CORS information (1 hour)
        maxAgeInSeconds = 3600
    }

    log.info("CORS configured")
}