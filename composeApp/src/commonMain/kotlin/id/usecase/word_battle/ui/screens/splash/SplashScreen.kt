package id.usecase.word_battle.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.word_battle.R
import id.usecase.word_battle.ui.theme.WordBattleTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // In a real app, we would check if user is logged in here
    // For now, just navigate to login after a delay
    LaunchedEffect(key1 = true) {
        delay(1500) // 1.5 second splash
        onNavigateToLogin()
        // If we had auth state check, we would use:
        // if (isLoggedIn) onNavigateToHome() else onNavigateToLogin()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
            ) {
                // Use a placeholder image
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo"
                )
            }

            Text(
                text = "Word Battle",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    WordBattleTheme {
        SplashScreen(
            onNavigateToLogin = {},
            onNavigateToHome = {}
        )
    }
}