@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package id.usecase.word_battle

import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual class Platform {
    actual val platform: String = "Android ${Build.VERSION.SDK_INT}"
    actual fun getDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

actual object PlatformLogger {
    actual fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }

    actual fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}

actual fun getPlatform(): Platform = Platform()