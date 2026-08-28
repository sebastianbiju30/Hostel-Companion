package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.Notice
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen() {
    var notices by remember { mutableStateOf<List<Notice>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                notices = SupabaseProvider.db.from("notices")
                    .select()
                    .decodeList<Notice>()
            } catch (e: Exception) {
                // Fallback sample notices
                notices = listOf(
                    Notice(
                        id = "1",
                        title = "Water Supply Maintenance",
                        body = "Water supply will be suspended on Saturday from 10:00 AM to 2:00 PM for tank cleaning.",
                        priority = "High"
                    ),
                    Notice(
                        id = "2",
                        title = "Hostel Annual Cultural Night",
                        body = "Join us for the annual hostel night in the main auditorium this Friday at 6:00 PM!",
                        priority = "Medium"
                    ),
                    Notice(
                        id = "3",
                        title = "Quiet Hours Enforcement",
                        body = "Strict quiet hours are in effect from 10:00 PM to 6:00 AM daily due to upcoming mid-terms.",
                        priority = "Low"
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notices & Announcements") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notices) { notice ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(notice.title, style = MaterialTheme.typography.titleMedium)
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("${notice.priority} Priority") }
                                    )
                                }
                                notice.body?.let { bodyText ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        bodyText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
