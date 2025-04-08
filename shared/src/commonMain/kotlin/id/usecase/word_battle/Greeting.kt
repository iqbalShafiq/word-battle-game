package id.usecase.word_battle

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.platform}!"
    }
}