@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package id.usecase.word_battle

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual class Platform {
    actual val platform: String = "Java ${System.getProperty("java.version")}"
    actual fun getDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

actual object PlatformLogger {
    actual fun debug(tag: String, message: String) {
        println("$tag: $message")
    }

    actual fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            println("$tag: $message")
            throwable.printStackTrace()
        } else {
            println("$tag: $message")
        }
    }
}

actual fun getPlatform(): Platform = Platform()