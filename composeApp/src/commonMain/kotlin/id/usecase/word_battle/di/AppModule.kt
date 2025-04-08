package id.usecase.word_battle.di

import android.content.Context
import android.content.SharedPreferences
import id.usecase.word_battle.ui.screens.auth.LoginViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Shared Preferences
    single<SharedPreferences> {
        androidContext().getSharedPreferences(
            "word_battle_token_prefs",
            Context.MODE_PRIVATE
        )
    }
}