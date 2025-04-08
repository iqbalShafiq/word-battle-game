package id.usecase.word_battle.di

import id.usecase.word_battle.auth.TokenManager
import id.usecase.word_battle.network.AuthApi
import id.usecase.word_battle.network.KtorClient
import id.usecase.word_battle.network.UserApi
import org.koin.dsl.module

val networkModule = module {
    single { KtorClient.create() }
    single { TokenManager(get()) }
    single { AuthApi(get()) }
    single { UserApi(get(), get()) }
}