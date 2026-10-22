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
import com.example.hostel_companion.data.models.LaundryOrder
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryScreen() {
    var orders by remember { mutableStateOf<List<LaundryOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun fetchLaundry() {
        scope.launch {
            isLoading = true
            try {
                orders = SupabaseProvider.db.from("laundry_orders").select().decodeList<LaundryOrder>()
            } catch (_: Exception) {
                orders = listOf(
                    LaundryOrder(id = "l101", itemsCount = 7, status = "Washing", droppedAt = "Yesterday, 04:30 PM"),
                    LaundryOrder(id = "l102", itemsCount = 4, status = "Ready", droppedAt = "Oct 20, 11:00 AM")
                )
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { fetchLaundry() }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hostel Laundry Service", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Track washing, drying & pickup collection", style = MaterialTheme.typography.bodySmall, color = AccentTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface),
                actions = {
                    IconButton(onClick = { fetchLaundry() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                containerColor = AccentTeal,
                contentColor = TextPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Drop Laundry", fontWeight = FontWeight.Bold) }
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentTeal)
            } else if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No laundry orders submitted yet.", color = TextMuted)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(orders) { order ->
                        val isReady = order.status.lowercase() in listOf("ready", "collected")
                        val isWashing = order.status.lowercase() == "washing"
                        val isDrying = order.status.lowercase() == "drying"

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
                                                    else if (isWashing) AccentBlue.copy(alpha = 0.2f)
                                                    else AccentAmber.copy(alpha = 0.2f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Checkroom,
                                                contentDescription = null,
                                                tint = if (isReady) StatusGreen else if (isWashing) AccentBlueLight else AccentAmber,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("${order.itemsCount} Clothes Dropped", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                            Text("Dropped: ${order.droppedAt ?: "Recently"}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                    }

                                    Surface(
                                        color = when {
                                            isReady -> StatusGreen.copy(alpha = 0.2f)
                                            isWashing -> AccentBlue.copy(alpha = 0.2f)
                                            else -> AccentAmber.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = order.status.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = when {
                                                isReady -> StatusGreen
                                                isWashing -> AccentBlueLight
                                                else -> AccentAmber
                                            },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (isReady) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        color = StatusGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.DoneAll, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ready for collection at Laundry Desk (Block B Basement)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = StatusGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                var count by remember { mutableStateOf("6") }
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Drop Clothes at Laundry Desk", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Specify the number of garments to hand over:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            OutlinedTextField(
                                value = count,
                                onValueChange = { count = it },
                                label = { Text("Number of Clothes (pieces)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDialog = false
                                scope.launch {
                                    try {
                                        val userId = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                                        val newOrder = LaundryOrder(
                                            id = UUID.randomUUID().toString(),
                                            studentId = userId,
                                            itemsCount = count.toIntOrNull() ?: 5,
                                            status = "Dropped",
                                            droppedAt = SimpleDateFormat("hh:mm a, dd MMM", Locale.US).format(Date())
                                        )
                                        try {
                                            SupabaseProvider.db.from("laundry_orders").insert(newOrder)
                                        } catch (_: Exception) {}
                                        orders = listOf(newOrder) + orders
                                    } catch (_: Exception) {}
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                        ) {
                            Text("Submit Order", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel", color = TextMuted) }
                    },
                    containerColor = DarkCardSurface
                )
            }
        }
    }
}
