package id.usecase.word_battle.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.usecase.word_battle.ui.components.PrimaryButton
import id.usecase.word_battle.ui.components.SecondaryButton
import id.usecase.word_battle.ui.components.StandardCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLobby: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Word Battle") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Word Battle!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            StandardCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Game Stats",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Games Played: 0")
                Text("Wins: 0")
                Text("Best Score: 0")
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Find Match",
                onClick = onNavigateToLobby,
                icon = Icons.Rounded.PlayArrow,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryButton(
                text = "Logout",
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}