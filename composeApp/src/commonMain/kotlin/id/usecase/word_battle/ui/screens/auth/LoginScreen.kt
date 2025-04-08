package id.usecase.word_battle.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.usecase.word_battle.ui.components.BorderedButton
import id.usecase.word_battle.ui.components.PasswordTextField
import id.usecase.word_battle.ui.components.PrimaryButton
import id.usecase.word_battle.ui.components.StandardTextField

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Word Battle",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Login to play with friends",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                StandardTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    leadingIcon = Icons.Filled.Person,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    imeAction = ImeAction.Done,
                    onImeAction = { onLoginSuccess() }
                )

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButton(
                    text = "Login",
                    onClick = onLoginSuccess,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                BorderedButton(
                    text = "Register",
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}