package id.usecase.word_battle.di

import org.koin.dsl.module

val appModule = module {
    // Will be populated as we add repositories, viewmodels, etc.

    // Network module - will be added later
    // includes { networkModule }

    // ViewModels - will be added later
    // viewModel { MainViewModel(get()) }

    // Repositories - will be added later
    // single { UserRepository(get()) }
}