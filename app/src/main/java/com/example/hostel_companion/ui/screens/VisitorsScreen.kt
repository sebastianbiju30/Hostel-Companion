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
import com.example.hostel_companion.data.models.Visitor
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorsScreen() {
    var visitors by remember { mutableStateOf<List<Visitor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun fetchVisitors() {
        scope.launch {
            isLoading = true
            try {
                visitors = SupabaseProvider.db.from("visitors").select().decodeList<Visitor>()
            } catch (_: Exception) {
                visitors = listOf(
                    Visitor(id = "1", visitorName = "John Doe", purpose = "Parent Visit", status = "Approved"),
                    Visitor(id = "2", visitorName = "Alex Smith", purpose = "Study Group", status = "Pending")
                )
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { fetchVisitors() }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Visitor Passes & Requests", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = AccentTeal) {
                Icon(Icons.Default.Add, contentDescription = "New Pass", tint = TextPrimary)
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
                    items(visitors) { visitor ->
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
                                    Text(visitor.visitorName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                    Text(visitor.purpose ?: "Hostel Visit", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                                Surface(
                                    color = if (visitor.status.lowercase() == "approved") Color(0xFF064E3B) else Color(0xFF451A03),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        visitor.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (visitor.status.lowercase() == "approved") AccentTeal else AccentAmber,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                var name by remember { mutableStateOf("") }
                var phone by remember { mutableStateOf("") }
                var purpose by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Request Visitor Pass") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Visitor Name") })
                            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Visitor Phone") })
                            OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Purpose of Visit") })
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (name.isNotBlank()) {
                                showDialog = false
                                scope.launch {
                                    try {
                                        val userId = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                                        SupabaseProvider.db.from("visitors").insert(
                                            Visitor(studentId = userId, visitorName = name, visitorPhone = phone, purpose = purpose)
                                        )
                                        fetchVisitors()
                                    } catch (_: Exception) {}
                                }
                            }
                        }) { Text("Submit Request") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
