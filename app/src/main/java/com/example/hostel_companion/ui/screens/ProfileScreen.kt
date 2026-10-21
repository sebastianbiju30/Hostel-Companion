package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.UserProfile
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onSignOut: () -> Unit
) {
    val user = SupabaseProvider.auth.currentUserOrNull()
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
var isAdminMode by remember { mutableStateOf(userProfile?.isAdmin ?: false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val userId = user?.id ?: ""
                userProfile = SupabaseProvider.db.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<UserProfile>()
            } catch (_: Exception) {}
        }
    }

    val isWarden = userProfile?.role?.lowercase() in listOf("admin", "warden")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile Avatar",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userProfile?.fullName ?: user?.email ?: "Student User",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isWarden) "Warden / Admin Authority" else "Hostel Resident • Block A",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Account Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Email: ${user?.email ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                    Text("Phone: ${userProfile?.phone ?: "Not provided"}", style = MaterialTheme.typography.bodySmall)
                    Text("Role: ${if (isWarden) "Warden / Admin" else "Student Resident"}", style = MaterialTheme.typography.bodySmall)
                    Text("User ID: ${user?.id ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Warden / Admin Control Panel Access
            Button(
                onClick = { navController.navigate("admin_dashboard") },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.SupervisorAccount, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isWarden) "Open Warden Control Panel" else "Switch to Warden / Admin Mode")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.navigate("onboarding") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Personal Details / Room")
            }

            // Admin Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Admin Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isAdminMode,
                    onCheckedChange = { checked ->
                        isAdminMode = checked
                        scope.launch {
                            try {
                                val userId = user?.id ?: ""
                                SupabaseProvider.db.from("profiles")
                                    .update({ set("is_admin", checked) }) {
                                        filter { eq("id", userId) }
                                    }
                                // Update local profile state
                                userProfile = userProfile?.copy(isAdmin = checked)
                            } catch (e: Exception) {
                                // Optional: handle error
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentBlueLight,
                        uncheckedThumbColor = TextMuted
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            SupabaseProvider.auth.signOut()
                        } catch (_: Exception) {}
                        onSignOut()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}
