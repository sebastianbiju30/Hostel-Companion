package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.Room
import com.example.hostel_companion.data.models.UserProfile
import com.example.hostel_companion.ui.theme.DarkBackground
import com.example.hostel_companion.ui.theme.DarkCardSurface
import com.example.hostel_companion.ui.theme.TextMuted
import com.example.hostel_companion.ui.theme.TextPrimary
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("A-304") }
    var block by remember { mutableStateOf("North Block") }
    var selectedRole by remember { mutableStateOf("student") } // "student" or "warden"
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val user = SupabaseProvider.auth.currentUserOrNull()

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
            Text(
                text = "Welcome to Hostel Companion! 👋",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please complete your personal details to personalize your account.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Personal Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = roomNumber,
                            onValueChange = { roomNumber = it },
                            label = { Text("Room No") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = block,
                            onValueChange = { block = it },
                            label = { Text("Block") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Select Account Role:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = selectedRole == "student",
                            onClick = { selectedRole = "student" },
                            label = { Text("Student Resident") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedRole == "warden" || selectedRole == "admin",
                            onClick = { selectedRole = "warden" },
                            label = { Text("Warden / Admin") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        errorMessage = "Please enter your full name."
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val userId = user?.id ?: ""
                            var createdRoomId: String? = null

                            // 1. Create room entry if provided
                            if (roomNumber.isNotBlank()) {
                                try {
                                    val existingRoom = SupabaseProvider.db.from("rooms")
                                        .select { filter { eq("room_number", roomNumber.trim()) } }
                                        .decodeSingleOrNull<Room>()

                                    if (existingRoom != null) {
                                        createdRoomId = existingRoom.id
                                    } else {
                                        val newRoom = SupabaseProvider.db.from("rooms")
                                            .insert(Room(roomNumber = roomNumber.trim(), block = block.trim())) {
                                                select()
                                            }.decodeSingle<Room>()
                                        createdRoomId = newRoom.id
                                    }
                                } catch (_: Exception) {}
                            }

                            // 2. Upsert profile details
                            val updatedProfile = UserProfile(
                                id = userId,
                                fullName = fullName.trim(),
                                email = user?.email ?: "",
                                phone = phone.trim(),
                                role = selectedRole,
                                roomId = createdRoomId
                            )

                            try {
                                SupabaseProvider.db.from("profiles").upsert(updatedProfile)
                            } catch (_: Exception) {}

                            onOnboardingComplete()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to save details."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Save & Continue to App")
                }
            }
        }
    }
}
