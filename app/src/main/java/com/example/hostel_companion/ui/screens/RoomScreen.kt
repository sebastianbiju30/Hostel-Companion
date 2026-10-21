package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.Room
import com.example.hostel_companion.data.models.UserProfile
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen() {
    var roomInfo by remember { mutableStateOf<Room?>(null) }
    var roommates by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredRoommates = if (searchQuery.isBlank()) roommates else roommates.filter {
        (it.fullName?.contains(searchQuery, ignoreCase = true) ?: false) ||
        (it.email?.contains(searchQuery, ignoreCase = true) ?: false)
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val userId = SupabaseProvider.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val profile = SupabaseProvider.db.from("profiles")
                        .select { filter { eq("id", userId) } }
                        .decodeSingleOrNull<UserProfile>()

                    if (profile?.roomId != null) {
                        roomInfo = SupabaseProvider.db.from("rooms")
                            .select { filter { eq("id", profile.roomId) } }
                            .decodeSingleOrNull<Room>()

                        roommates = SupabaseProvider.db.from("profiles")
                            .select { filter { eq("room_id", profile.roomId) } }
                            .decodeList<UserProfile>()
                    }
                }
            } catch (_: Exception) {
                // Fallback mock room data if offline
            } finally {
                if (roomInfo == null) {
                    roomInfo = Room(id = "1", roomNumber = "A-304", block = "North Block", capacity = 2)
                    roommates = listOf(
                        UserProfile(fullName = "Sebastian (You)", email = "sebastianbiju30@gmail.com"),
                        UserProfile(fullName = "John Doe", email = "john.doe@example.com")
                    )
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("My Room Details", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Domain, contentDescription = null, tint = AccentBlueLight, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Room ${roomInfo?.roomNumber}", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                                    Text(roomInfo?.block ?: "North Block", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = DarkCardSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Capacity: ${roomInfo?.capacity} Beds", color = TextMuted)
                                Text("Occupied: ${filteredRoommates.size} Beds", color = AccentTeal)
                            }
                        }
                    }

                    Text("Roommates", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search roommates") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                    filteredRoommates.forEach { roommate ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlueLight)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(roommate.fullName ?: "Student Resident", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(roommate.email ?: "", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
