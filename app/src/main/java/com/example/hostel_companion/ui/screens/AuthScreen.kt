package com.example.hostel_companion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.UserProfile
import com.example.hostel_companion.data.parseSupabaseErrorMessage
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onLoginSuccess: (isNewUser: Boolean, role: String) -> Unit
) {
    // 0: Student Portal, 1: Warden / Admin Portal
    var selectedPortal by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var wardenPasscode by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            if (selectedPortal == 0) listOf(AccentBlue, AccentTeal) else listOf(AccentAmber, AccentRed)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (selectedPortal == 0) Icons.Default.School else Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hostel Companion",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (selectedPortal == 0) "Resident Student Portal" else "Warden & Administration Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedPortal == 0) AccentTeal else AccentAmber,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PORTAL SELECTOR TABS (Completely separates Student vs Warden)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = DarkCardSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedPortal == 0) AccentBlue else Color.Transparent)
                            .clickable {
                                selectedPortal = 0
                                isSignUp = false
                                errorMessage = null
                                infoMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (selectedPortal == 0) Color.White else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Student Login",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedPortal == 0) Color.White else TextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedPortal == 1) AccentAmber else Color.Transparent)
                            .clickable {
                                selectedPortal = 1
                                isSignUp = false
                                errorMessage = null
                                infoMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.SupervisorAccount,
                                contentDescription = null,
                                tint = if (selectedPortal == 1) Color.Black else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Warden Portal",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedPortal == 1) Color.Black else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null; infoMessage = null },
                label = { Text(if (selectedPortal == 0) "Student Email" else "Warden Official Email") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkCardSurface,
                    unfocusedContainerColor = DarkCardSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = if (selectedPortal == 0) AccentBlueLight else AccentAmber
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null; infoMessage = null },
                label = { Text("Password (min 6 characters)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkCardSurface,
                    unfocusedContainerColor = DarkCardSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = if (selectedPortal == 0) AccentBlueLight else AccentAmber
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Warden Security Passcode (For warden portal verification)
            AnimatedVisibility(visible = selectedPortal == 1 && isSignUp) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = wardenPasscode,
                        onValueChange = { wardenPasscode = it },
                        label = { Text("Warden Passcode (Use 'WARDEN2026')") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCardSurface,
                            unfocusedContainerColor = DarkCardSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentAmber
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            infoMessage?.let { msg ->
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
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

            Spacer(modifier = Modifier.height(20.dp))

            // Sign In / Sign Up Button
            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    val targetRole = if (selectedPortal == 1) "warden" else "student"

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
                    if (selectedPortal == 1 && isSignUp && wardenPasscode.trim() != "WARDEN2026") {
                        errorMessage = "Invalid Warden Security Passcode. Default is WARDEN2026"
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
                                        SupabaseProvider.auth.signInWith(Email) {
                                            this.email = trimmedEmail
                                            this.password = trimmedPassword
                                        }
                                    } else {
                                        throw e
                                    }
                                }

                                try {
                                    SupabaseProvider.auth.signInWith(Email) {
                                        this.email = trimmedEmail
                                        this.password = trimmedPassword
                                    }
                                    val uid = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                                    // Update role in profiles
                                    if (uid.isNotBlank()) {
                                        SupabaseProvider.db.from("profiles")
                                            .update({
                                                set("role", targetRole)
                                                set("is_admin", selectedPortal == 1)
                                            }) {
                                                filter { eq("id", uid) }
                                            }
                                    }
                                    onLoginSuccess(true, targetRole)
                                } catch (_: Exception) {
                                    infoMessage = "Account created! Please sign in with your credentials."
                                    isSignUp = false
                                }
                            } else {
                                SupabaseProvider.auth.signInWith(Email) {
                                    this.email = trimmedEmail
                                    this.password = trimmedPassword
                                }

                                // Check user profile role
                                val uid = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                                var userRole = targetRole
                                try {
                                    val profile = SupabaseProvider.db.from("profiles")
                                        .select { filter { eq("id", uid) } }
                                        .decodeSingleOrNull<UserProfile>()
                                    if (profile != null) {
                                        userRole = profile.role.lowercase()
                                        if (profile.isAdmin) userRole = "warden"
                                    }
                                } catch (_: Exception) {}

                                // Prevent student from entering warden portal without warden role
                                if (selectedPortal == 1 && userRole !in listOf("warden", "admin")) {
                                    errorMessage = "Access Denied: This account does not have Warden/Admin privileges."
                                    SupabaseProvider.auth.signOut()
                                    return@launch
                                }

                                onLoginSuccess(false, userRole)
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPortal == 0) AccentBlue else AccentAmber,
                    contentColor = if (selectedPortal == 0) Color.White else Color.Black
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = if (selectedPortal == 0) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        if (isSignUp) "Register ${if (selectedPortal == 0) "Student" else "Warden"}"
                        else "Sign In as ${if (selectedPortal == 0) "Student" else "Warden"}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick One-Tap Demo Login Button for easy testing
            OutlinedButton(
                onClick = {
                    val demoEmail = if (selectedPortal == 0) "student@hostel.edu" else "warden@hostel.edu"
                    val demoPass = if (selectedPortal == 0) "Student@123" else "Warden@2026!"
                    val demoRole = if (selectedPortal == 0) "student" else "warden"

                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        infoMessage = null
                        try {
                            try {
                                SupabaseProvider.auth.signInWith(Email) {
                                    this.email = demoEmail
                                    this.password = demoPass
                                }
                            } catch (_: Exception) {
                                SupabaseProvider.auth.signUpWith(Email) {
                                    this.email = demoEmail
                                    this.password = demoPass
                                }
                                SupabaseProvider.auth.signInWith(Email) {
                                    this.email = demoEmail
                                    this.password = demoPass
                                }
                            }

                            val uid = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                            if (uid.isNotBlank()) {
                                try {
                                    SupabaseProvider.db.from("profiles")
                                        .update({
                                            set("role", demoRole)
                                            set("is_admin", selectedPortal == 1)
                                            if (selectedPortal == 1) set("full_name", "Chief Warden Dr. Sharma")
                                            else set("full_name", "Sebastian Biju (Resident)")
                                        }) {
                                            filter { eq("id", uid) }
                                        }
                                } catch (_: Exception) {}
                            }

                            onLoginSuccess(false, demoRole)
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
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkCardSurfaceVariant)
                )
            ) {
                Text(
                    if (selectedPortal == 0) "⚡ Quick Student Demo Login" else "👑 Quick Warden Demo Login",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { isSignUp = !isSignUp; errorMessage = null; infoMessage = null }) {
                Text(
                    if (isSignUp) "Already registered? Sign In" else "New to Hostel? Register Account",
                    color = TextCyan
                )
            }
        }
    }
}
