package id.usecase.evaluasi.dotenv

import io.github.cdimascio.dotenv.Dotenv

object DotenvConfig {
    val dotenv: Dotenv = createDotenv()

    private fun createDotenv(): Dotenv {
        val runningInDocker = System.getenv("RUNNING_IN_DOCKER")?.toBoolean() == true
        val dotenvDir = if (runningInDocker) "/app" else "./"

        return Dotenv.configure()
            .directory("./")
            .filename(".env")
            .load()
    }
}
