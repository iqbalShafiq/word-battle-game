@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package id.usecase.word_battle

import kotlinx.coroutines.CoroutineDispatcher

expect class Platform {
    val platform: String
    fun getDispatcher(): CoroutineDispatcher
}

expect object PlatformLogger {
    fun debug(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

expect fun getPlatform(): Platform