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
import androidx.compose.ui.unit.sp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.Visitor
import com.example.hostel_companion.data.repository.HostelRepository
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorsScreen() {
    val visitors by HostelRepository.visitors.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        HostelRepository.refreshFromSupabase()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Visitor Passes & Gate Approvals", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Approved passes verified at security gate", style = MaterialTheme.typography.bodySmall, color = AccentTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface),
                actions = {
                    IconButton(onClick = { HostelRepository.refreshFromSupabase() }) {
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
                text = { Text("Request Pass", fontWeight = FontWeight.Bold) }
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
            } else if (visitors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No visitor passes requested yet.", color = TextMuted)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(visitors) { visitor ->
                        val isApproved = visitor.status.equals("approved", true)
                        val isRejected = visitor.status.equals("rejected", true)

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
                                                    if (isApproved) StatusGreen.copy(alpha = 0.2f)
                                                    else if (isRejected) StatusRed.copy(alpha = 0.2f)
                                                    else AccentAmber.copy(alpha = 0.2f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (isApproved) Icons.Default.CheckCircle else if (isRejected) Icons.Default.Cancel else Icons.Default.HourglassTop,
                                                contentDescription = null,
                                                tint = if (isApproved) StatusGreen else if (isRejected) StatusRed else AccentAmber,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(visitor.visitorName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                            Text("Contact: ${visitor.visitorPhone ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                    }

                                    Surface(
                                        color = if (isApproved) StatusGreen.copy(alpha = 0.2f) else if (isRejected) StatusRed.copy(alpha = 0.2f) else AccentAmber.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = visitor.status.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isApproved) StatusGreen else if (isRejected) StatusRed else AccentAmber,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Purpose: ${visitor.purpose ?: "Hostel Visit"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )

                                // Warden Approval Confirmation Banner
                                if (isApproved) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusGreen.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Warden Approved • Security Pass ID: #PASS-${visitor.id.take(6).uppercase()}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = StatusGreen
                                            )
                                        }
                                    }
                                } else if (isRejected) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusRed.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = StatusRed, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Pass Declined by Warden Office. Please visit warden office for queries.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StatusRed
                                            )
                                        }
                                    }
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
                    title = { Text("Request New Visitor Pass", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Submit guest details for warden review & gate pass clearance:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Visitor Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Visitor Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Purpose of Visit (Family / Project / Academic)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    showDialog = false
                                    val userId = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                                    val newV = Visitor(
                                        id = UUID.randomUUID().toString(),
                                        studentId = userId,
                                        visitorName = name.trim(),
                                        visitorPhone = phone.trim(),
                                        purpose = purpose.trim(),
                                        status = "Pending",
                                        requestedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                                    )
                                    HostelRepository.addVisitor(newV)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                        ) {
                            Text("Submit to Warden", fontWeight = FontWeight.Bold)
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
