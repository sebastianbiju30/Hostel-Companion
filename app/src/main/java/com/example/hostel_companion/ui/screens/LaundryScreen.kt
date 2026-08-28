package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.LaundryOrder
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

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
                    LaundryOrder(id = "1", itemsCount = 5, status = "Washing"),
                    LaundryOrder(id = "2", itemsCount = 3, status = "Ready")
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
                title = { Text("Laundry Orders", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = AccentTeal) {
                Icon(Icons.Default.Add, contentDescription = "Drop Laundry", tint = TextPrimary)
            }
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${order.itemsCount} Clothes Dropped", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                    Text("Order #${order.id.take(6)}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                                Surface(
                                    color = when (order.status.lowercase()) {
                                        "ready", "collected" -> Color(0xFF064E3B)
                                        else -> Color(0xFF1E293B)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        order.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentTeal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                var count by remember { mutableStateOf("5") }
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Drop Laundry") },
                    text = {
                        OutlinedTextField(
                            value = count,
                            onValueChange = { count = it },
                            label = { Text("Number of Clothes") }
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            scope.launch {
                                try {
                                    val userId = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                                    SupabaseProvider.db.from("laundry_orders").insert(
                                        LaundryOrder(itemsCount = count.toIntOrNull() ?: 1, studentId = userId)
                                    )
                                    fetchLaundry()
                                } catch (_: Exception) {}
                            }
                        }) {
                            Text("Submit Order")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
