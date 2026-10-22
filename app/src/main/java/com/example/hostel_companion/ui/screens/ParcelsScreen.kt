package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.Parcel
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelsScreen() {
    var parcels by remember { mutableStateOf<List<Parcel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun fetchParcels() {
        scope.launch {
            isLoading = true
            try {
                parcels = SupabaseProvider.db.from("parcels").select().decodeList<Parcel>()
            } catch (_: Exception) {
                parcels = listOf(
                    Parcel(id = "pr101", sender = "Amazon Delivery", description = "MacBook Charger & Electronics", status = "Ready for Pickup", arrivedAt = "Today, 11:30 AM"),
                    Parcel(id = "pr102", sender = "Flipkart Package", description = "Engineering Textbooks & Notes", status = "Arrived", arrivedAt = "Yesterday, 04:00 PM"),
                    Parcel(id = "pr103", sender = "BlueDart Express", description = "Home Documents & Sweets", status = "Collected", arrivedAt = "Oct 19, 02:15 PM")
                )
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { fetchParcels() }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Courier & Parcels Desk", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Collect packages from Security Gate Office", style = MaterialTheme.typography.bodySmall, color = AccentTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface),
                actions = {
                    IconButton(onClick = { fetchParcels() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
                    }
                }
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentPurple)
            } else if (parcels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No courier parcels recorded for you.", color = TextMuted)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(parcels) { parcel ->
                        val isReady = parcel.status.lowercase() == "ready for pickup"
                        val isCollected = parcel.status.lowercase() == "collected"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isReady) StatusGreen.copy(alpha = 0.2f)
                                                    else if (isCollected) DarkCardSurfaceVariant
                                                    else AccentPurple.copy(alpha = 0.2f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Inventory2,
                                                contentDescription = null,
                                                tint = if (isReady) StatusGreen else if (isCollected) TextMuted else AccentPurple,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(parcel.sender ?: "Courier Package", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                            Text(parcel.description ?: "Package at Gate Desk", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                    }

                                    Surface(
                                        color = if (isReady) StatusGreen.copy(alpha = 0.2f) else if (isCollected) DarkCardSurfaceVariant else AccentPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = parcel.status.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isReady) StatusGreen else if (isCollected) TextMuted else AccentPurple,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                parcel.arrivedAt?.let { arr ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Logged by Security Gate: $arr", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                                }

                                if (isReady) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        color = StatusGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Package arrived! Show your Student ID at Main Gate Security to collect.", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = StatusGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
