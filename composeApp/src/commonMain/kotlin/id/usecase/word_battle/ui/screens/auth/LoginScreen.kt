package id.usecase.word_battle.ui.screens.auth

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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.word_battle.ui.components.BorderedButton
import id.usecase.word_battle.ui.components.LoadingDialog
import id.usecase.word_battle.ui.components.PasswordTextField
import id.usecase.word_battle.ui.components.PrimaryButton
import id.usecase.word_battle.ui.components.StandardTextField
import id.usecase.word_battle.ui.theme.WordBattleTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> {
                    onLoginSuccess()
                }
                is LoginEffect.NavigateToRegister -> {
                    onNavigateToRegister()
                }
                is LoginEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

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
                    value = state.username,
                    onValueChange = { viewModel.processIntent(LoginIntent.UpdateUsername(it)) },
                    label = "Username",
                    leadingIcon = Icons.Filled.Person,
                    errorMessage = state.usernameError,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = state.password,
                    onValueChange = { viewModel.processIntent(LoginIntent.UpdatePassword(it)) },
                    errorMessage = state.passwordError,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        viewModel.processIntent(LoginIntent.Login)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButton(
                    text = "Login",
                    onClick = { viewModel.processIntent(LoginIntent.Login) },
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                BorderedButton(
                    text = "Register",
                    onClick = { viewModel.processIntent(LoginIntent.NavigateToRegister) },
                    isEnabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Loading dialog
            LoadingDialog(
                isVisible = state.isLoading,
                message = "Logging in..."
            )
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    WordBattleTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}