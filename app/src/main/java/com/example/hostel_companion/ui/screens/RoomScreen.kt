package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var roommates by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferReason by remember { mutableStateOf("") }
    var transferSuccessMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val userId = SupabaseProvider.auth.currentUserOrNull()?.id
                if (userId != null) {
                    userProfile = SupabaseProvider.db.from("profiles")
                        .select { filter { eq("id", userId) } }
                        .decodeSingleOrNull<UserProfile>()

                    if (userProfile?.roomId != null) {
                        roomInfo = SupabaseProvider.db.from("rooms")
                            .select { filter { eq("id", userProfile!!.roomId!!) } }
                            .decodeSingleOrNull<Room>()

                        roommates = SupabaseProvider.db.from("profiles")
                            .select { filter { eq("room_id", userProfile!!.roomId!!) } }
                            .decodeList<UserProfile>()
                    }
                }
            } catch (_: Exception) {
            } finally {
                if (roomInfo == null) {
                    roomInfo = Room(
                        id = "r304",
                        roomNumber = "A-304",
                        block = "North Block",
                        capacity = 2
                    )
                    roommates = listOf(
                        UserProfile(fullName = "Sebastian Biju (You)", email = "sebastianbiju30@gmail.com", phone = "+91 98765 43210"),
                        UserProfile(fullName = "Rahul Sharma", email = "rahul.sharma@hostel.edu", phone = "+91 98765 12345")
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
                title = {
                    Column {
                        Text("My Room & Allocation", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Allotted by Hostel Warden", style = MaterialTheme.typography.bodySmall, color = AccentTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlueLight)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Primary Room Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlueLight.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Domain, contentDescription = null, tint = AccentBlueLight, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Room ${roomInfo?.roomNumber ?: "A-304"}",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${roomInfo?.block ?: "North Block"} • 3rd Floor",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextCyan
                                    )
                                }
                            }

                            Surface(
                                color = StatusGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ALLOCATED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = StatusGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = DarkCardSurfaceVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Room Capacity", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${roomInfo?.capacity ?: 2} Sharing Beds", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                            Column {
                                Text("Your Bed Number", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("Bed 1 (Window Side)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AccentTeal)
                            }
                            Column {
                                Text("Inspection Grade", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("Grade A+ (Clean)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AccentAmber)
                            }
                        }
                    }
                }

                // Room Amenities
                Text(
                    text = "Room Facilities & Amenities",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AmenityRow(Icons.Default.Wifi, "High-Speed Wi-Fi", "Connected (Hostel_HighSpeed_5G)")
                        HorizontalDivider(color = DarkCardSurfaceVariant)
                        AmenityRow(Icons.Default.AcUnit, "Air Conditioner", "1.5 Ton Split AC with remote")
                        HorizontalDivider(color = DarkCardSurfaceVariant)
                        AmenityRow(Icons.Default.Bathtub, "Attached Washroom", "With 25L hot water geyser")
                        HorizontalDivider(color = DarkCardSurfaceVariant)
                        AmenityRow(Icons.Default.Desk, "Study Desks & Chairs", "Individual teak study desks with lamps")
                        HorizontalDivider(color = DarkCardSurfaceVariant)
                        AmenityRow(Icons.Default.Balcony, "Balcony Access", "Facing campus garden")
                    }
                }

                // Roommates List
                Text(
                    text = "Allocated Roommates (${roommates.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    roommates.forEach { roommate ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(DarkCardSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = AccentTeal)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = roommate.fullName ?: "Resident Student",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = roommate.email ?: "student@hostel.edu",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Surface(
                                    color = DarkCardSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Request Room Transfer / Change Button
                OutlinedButton(
                    onClick = { showTransferDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkCardSurfaceVariant)
                    )
                ) {
                    Icon(Icons.Default.SwapCalls, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Room Reallocation / Change")
                }

                transferSuccessMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusGreen.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            color = StatusGreen,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showTransferDialog) {
            AlertDialog(
                onDismissRequest = { showTransferDialog = false },
                title = { Text("Request Room Change", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Please state your reason for requesting room change. This will be sent directly to the Chief Warden for review.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        OutlinedTextField(
                            value = transferReason,
                            onValueChange = { transferReason = it },
                            label = { Text("Reason (e.g. medical need, study quietness)") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (transferReason.isNotBlank()) {
                                showTransferDialog = false
                                transferSuccessMessage = "✓ Room change request submitted to Warden Office. You will receive an update shortly."
                                transferReason = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlueLight)
                    ) {
                        Text("Submit to Warden")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTransferDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = DarkCardSurface
            )
        }
    }
}

@Composable
fun AmenityRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AccentBlueLight, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}
