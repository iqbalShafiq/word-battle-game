package id.usecase.word_battle.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import id.usecase.word_battle.ui.screens.auth.LoginScreen
import id.usecase.word_battle.ui.screens.auth.RegisterScreen
import id.usecase.word_battle.ui.screens.demo.ComponentsDemoScreen
import id.usecase.word_battle.ui.screens.game.GameScreen
import id.usecase.word_battle.ui.screens.home.HomeScreen
import id.usecase.word_battle.ui.screens.lobby.LobbyScreen
import id.usecase.word_battle.ui.screens.profile.ProfileScreen
import id.usecase.word_battle.ui.screens.splash.SplashScreen

/**
 * Main navigation graph for the app
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Splash
    ) {
        // Splash screen - entry point
        composable<Splash> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Login) {
                        popUpTo(Splash) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }

        // Auth screens
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Home) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Register)
                }
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Home) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Main screens
        composable<Home> {
            HomeScreen(
                onNavigateToLobby = { navController.navigate(Lobby) },
                onNavigateToProfile = { navController.navigate(Profile) },
                onLogout = {
                    navController.navigate(Login) {
                        popUpTo(Home) { inclusive = true }
                    }
                }
            )
        }

        composable<Lobby> {
            LobbyScreen(
                onNavigateBack = { navController.popBackStack() },
                onGameFound = { gameId ->
                    navController.navigate(Game(gameId))
                }
            )
        }

        composable<Game> { backStackEntry ->
            val gameRoom: Game = backStackEntry.toRoute()
            val gameId = gameRoom.gameId
            GameScreen(gameId = gameId) {
                navController.popBackStack(Home, false)
            }
        }

        composable<Profile> {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<ComponentsDemo> {
            ComponentsDemoScreen()
        }
    }
}