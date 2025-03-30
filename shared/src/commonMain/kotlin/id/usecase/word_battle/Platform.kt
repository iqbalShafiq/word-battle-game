package id.usecase.word_battle

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform