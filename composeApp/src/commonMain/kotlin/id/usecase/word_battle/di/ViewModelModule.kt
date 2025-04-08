package id.usecase.word_battle.di

import id.usecase.word_battle.ui.screens.auth.LoginViewModel
import id.usecase.word_battle.ui.screens.auth.RegisterViewModel
import id.usecase.word_battle.ui.screens.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ProfileViewModel)
}