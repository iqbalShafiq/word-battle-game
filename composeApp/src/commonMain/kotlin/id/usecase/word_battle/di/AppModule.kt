package id.usecase.word_battle.di

import id.usecase.word_battle.data.repository.AuthRepositoryImpl
import id.usecase.word_battle.data.repository.GameRepositoryImpl
import id.usecase.word_battle.domain.repository.AuthRepository
import id.usecase.word_battle.domain.repository.GameRepository
import id.usecase.word_battle.ui.screens.auth.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl() }
    single<GameRepository> { GameRepositoryImpl() }

    // ViewModels
    viewModelOf(::LoginViewModel)
}