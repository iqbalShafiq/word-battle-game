package id.usecase.word_battle.di

import id.usecase.word_battle.data.repository.AuthRepositoryImpl
import id.usecase.word_battle.data.repository.GameRepositoryImpl
import id.usecase.word_battle.domain.repository.AuthRepository
import id.usecase.word_battle.domain.repository.GameRepository
import org.koin.dsl.module

val repositoryModule = module {
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<GameRepository> { GameRepositoryImpl(get(), get()) }
}