package id.usecase.word_battle.ui.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.word_battle.ui.components.LoadingDialog
import id.usecase.word_battle.ui.components.PasswordTextField
import id.usecase.word_battle.ui.components.PrimaryButton
import id.usecase.word_battle.ui.components.StandardTextField
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegisterEffect.NavigateToHome -> {
                    onRegisterSuccess()
                }

                is RegisterEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is RegisterEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(RegisterIntent.NavigateBack) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Create your account",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                StandardTextField(
                    value = state.username,
                    onValueChange = { viewModel.processIntent(RegisterIntent.UpdateUsername(it)) },
                    label = "Username",
                    leadingIcon = Icons.Filled.Person,
                    errorMessage = state.usernameError,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = state.password,
                    onValueChange = { viewModel.processIntent(RegisterIntent.UpdatePassword(it)) },
                    errorMessage = state.passwordError,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = state.confirmPassword,
                    onValueChange = {
                        viewModel.processIntent(
                            RegisterIntent.UpdateConfirmPassword(
                                it
                            )
                        )
                    },
                    label = "Confirm Password",
                    errorMessage = state.confirmPasswordError,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        viewModel.processIntent(RegisterIntent.Register)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButton(
                    text = "Create Account",
                    onClick = { viewModel.processIntent(RegisterIntent.Register) },
                    isLoading = state.isLoading,
                    isEnabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Loading dialog
            LoadingDialog(
                isVisible = state.isLoading,
                message = "Creating account..."
            )
        }
    }
}