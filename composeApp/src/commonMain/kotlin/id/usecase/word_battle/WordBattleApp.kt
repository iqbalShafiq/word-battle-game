package id.usecase.word_battle

import android.app.Application
import id.usecase.word_battle.di.appModule
import id.usecase.word_battle.di.networkModule
import id.usecase.word_battle.di.repositoryModule
import id.usecase.word_battle.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class WordBattleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@WordBattleApp)
            modules(appModule, networkModule, repositoryModule, viewModelModule)
        }
    }
}