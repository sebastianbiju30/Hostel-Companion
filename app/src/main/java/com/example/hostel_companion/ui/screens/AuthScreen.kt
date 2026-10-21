package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.parseSupabaseErrorMessage
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onLoginSuccess: (isNewUser: Boolean) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignUp) "Create Account" else "Hostel Companion Login",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Welcome! Sign in to manage your room & complaints",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null; infoMessage = null },
                label = { Text("Email Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    autoCorrectEnabled = false
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null; infoMessage = null },
                label = { Text("Password (min 6 characters)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )

            infoMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                        errorMessage = "Please enter both email and password"
                        return@Button
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                        errorMessage = "Please enter a valid email address"
                        return@Button
                    }
                    if (trimmedPassword.length < 6) {
                        errorMessage = "Password must be at least 6 characters long"
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        infoMessage = null
                        try {
                            if (isSignUp) {
                                try {
                                    SupabaseProvider.auth.signUpWith(Email) {
                                        this.email = trimmedEmail
                                        this.password = trimmedPassword
                                    }
                                } catch (e: Exception) {
                                    val errStr = e.message?.lowercase() ?: ""
                                    if (errStr.contains("already registered") || errStr.contains("user_already_exists")) {
                                        // Attempt sign-in if account exists
                                        SupabaseProvider.auth.signInWith(Email) {
                                            this.email = trimmedEmail
                                            this.password = trimmedPassword
                                        }
                                    } else {
                                        throw e
                                    }
                                }

                                // Perform sign in to establish active session and open Onboarding window
                                try {
                                    SupabaseProvider.auth.signInWith(Email) {
                                        this.email = trimmedEmail
                                        this.password = trimmedPassword
                                    }
                                    onLoginSuccess(true)
                                } catch (_: Exception) {
                                    if (SupabaseProvider.auth.currentSessionOrNull() != null) {
                                        onLoginSuccess(true)
                                    } else {
                                        infoMessage = "Account created! Please sign in with your credentials."
                                        isSignUp = false
                                    }
                                }
                            } else {
                                SupabaseProvider.auth.signInWith(Email) {
                                    this.email = trimmedEmail
                                    this.password = trimmedPassword
                                }
                                onLoginSuccess(false)
                            }
                        } catch (e: Exception) {
                            errorMessage = parseSupabaseErrorMessage(e)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(if (isSignUp) "Sign Up" else "Sign In")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        infoMessage = null
                        try {
                            try {
                                SupabaseProvider.auth.signInWith(Email) {
                                    this.email = "testuser99@gmail.com"
                                    this.password = "Password123!"
                                }
                            } catch (_: Exception) {
                                SupabaseProvider.auth.signUpWith(Email) {
                                    this.email = "testuser99@gmail.com"
                                    this.password = "Password123!"
                                }
                                SupabaseProvider.auth.signInWith(Email) {
                                    this.email = "testuser99@gmail.com"
                                    this.password = "Password123!"
                                }
                            }
                            onLoginSuccess(false)
                        } catch (e: Exception) {
                            errorMessage = parseSupabaseErrorMessage(e)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                Text("⚡ Quick Demo Sign-In")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { isSignUp = !isSignUp; errorMessage = null; infoMessage = null }) {
                Text(if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up")
            }
        }
    }
}


