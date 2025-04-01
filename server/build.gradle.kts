plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "id.usecase.word_battle"
version = "1.0.0"
application {
    mainClass.set("id.usecase.word_battle.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "true"}")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
        }
    }
}

dependencies {
    // shared
    implementation(projects.shared)

    // ktor server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)

    // logging
    implementation(libs.logback)

    // database
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.hikaricp)
    implementation(libs.postgresql)
    implementation(libs.flyway.core)

    // dependency injection
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // dotenv
    implementation(libs.dotenv.kotlin)

    // testing
    testImplementation(libs.kotlin.test.junit)
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}