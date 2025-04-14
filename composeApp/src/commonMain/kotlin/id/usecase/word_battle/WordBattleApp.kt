package id.usecase.word_battle

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import id.usecase.word_battle.di.appModule
import id.usecase.word_battle.di.networkModule
import id.usecase.word_battle.di.repositoryModule
import id.usecase.word_battle.di.viewModelModule
import id.usecase.word_battle.lifecycle.AppLifecycleObserver
import id.usecase.word_battle.network.WebSocketManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class WordBattleApp : Application() {
    private lateinit var lifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@WordBattleApp)
            modules(appModule, networkModule, repositoryModule, viewModelModule)
        }

        val webSocketManager: WebSocketManager by inject()
        lifecycleObserver = AppLifecycleObserver(webSocketManager)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    override fun onTerminate() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        super.onTerminate()
    }
}