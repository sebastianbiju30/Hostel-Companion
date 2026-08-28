package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.Complaint
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen() {
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun fetchComplaints() {
        scope.launch {
            isLoading = true
            try {
                complaints = SupabaseProvider.db.from("complaints")
                    .select()
                    .decodeList<Complaint>()
            } catch (e: Exception) {
                // Fallback sample data if DB connection is offline
                complaints = listOf(
                    Complaint(
                        id = "1",
                        title = "Fan not working",
                        description = "Ceiling fan making strange noise and stopped moving.",
                        category = "Electrical",
                        status = "Submitted"
                    ),
                    Complaint(
                        id = "2",
                        title = "Water tap leakage",
                        description = "Bathroom sink tap dripping continuously.",
                        category = "Plumbing",
                        status = "In Progress"
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchComplaints()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaints & Support") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Complaint")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (complaints.isEmpty()) {
                Text(
                    text = "No complaints logged yet.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(complaints) { complaint ->
                        ComplaintCard(complaint)
                    }
                }
            }

            if (showDialog) {
                AddComplaintDialog(
                    onDismiss = { showDialog = false },
                    onSubmit = { title, desc, cat ->
                        showDialog = false
                        scope.launch {
                            try {
                                val userId = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                                val newComplaint = Complaint(
                                    title = title,
                                    description = desc,
                                    category = cat,
                                    studentId = userId
                                )
                                SupabaseProvider.db.from("complaints").insert(newComplaint)
                                fetchComplaints()
                            } catch (e: Exception) {
                                complaints = complaints + Complaint(
                                    title = title,
                                    description = desc,
                                    category = cat
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ComplaintCard(complaint: Complaint) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(complaint.title, style = MaterialTheme.typography.titleMedium)
                StatusBadge(status = complaint.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Category: ${complaint.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            complaint.description?.let { desc ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(desc, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val containerColor = when (status.lowercase()) {
        "resolved" -> MaterialTheme.colorScheme.primaryContainer
        "in progress", "assigned" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun AddComplaintDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, desc: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electrical") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Complaint") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    maxLines = 3
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Electrical, Plumbing, AC, etc.)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSubmit(title, desc, category)
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
