package id.usecase.word_battle.di

import id.usecase.word_battle.data.repository.GameRepository
import id.usecase.word_battle.data.repository.GameRepositoryImpl
import id.usecase.word_battle.data.repository.PlayerRepository
import id.usecase.word_battle.data.repository.PlayerRepositoryImpl
import id.usecase.word_battle.data.repository.WordRepository
import id.usecase.word_battle.data.repository.WordRepositoryImpl
import id.usecase.word_battle.game.GameRoomManager
import id.usecase.word_battle.game.LetterGenerator
import id.usecase.word_battle.game.ScoringSystem
import id.usecase.word_battle.service.DictionaryService
import id.usecase.word_battle.service.GameService
import id.usecase.word_battle.service.MatchmakingService
import id.usecase.word_battle.service.UserService
import id.usecase.word_battle.websocket.WebSocketController
import org.koin.dsl.module

val serverModule = module {
    // Repositories (these will need implementations later)
    single<PlayerRepository> { PlayerRepositoryImpl() }
    single<GameRepository> { GameRepositoryImpl() }
    single<WordRepository> { WordRepositoryImpl() }

    // Game components
    single { LetterGenerator() }
    single { ScoringSystem() }
    single { GameRoomManager() }

    // Services
    single { UserService(get()) }
    single { GameService(get(), get()) }
    single { DictionaryService(get()) }
    single { MatchmakingService(get(), get()) }

    // WebSocket components
    single { WebSocketController() }
}