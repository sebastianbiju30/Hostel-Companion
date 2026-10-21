package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.*
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Complaints, 1: Notices, 2: Events, 3: Mess, 4: Visitors
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var notices by remember { mutableStateOf<List<Notice>>(emptyList()) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var visitors by remember { mutableStateOf<List<Visitor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog states
    var showNoticeDialog by remember { mutableStateOf(false) }
    var showEventDialog by remember { mutableStateOf(false) }
    var showMessDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun refreshData() {
        scope.launch {
            isLoading = true
            try {
                complaints = SupabaseProvider.db.from("complaints").select().decodeList<Complaint>()
            } catch (_: Exception) {
                complaints = listOf(
                    Complaint(id = "1", title = "Fan Noise A-304", category = "Electrical", status = "In Progress", description = "Ceiling fan making strange noise."),
                    Complaint(id = "2", title = "Water Leakage B-102", category = "Plumbing", status = "Submitted", description = "Sink tap leaking continuously.")
                )
            }

            try {
                notices = SupabaseProvider.db.from("notices").select().decodeList<Notice>()
            } catch (_: Exception) {
                notices = listOf(Notice(id = "1", title = "Water Supply Maintenance", body = "Water supply will be interrupted tomorrow 10am-2pm.", priority = "High"))
            }

            try {
                events = SupabaseProvider.db.from("events").select().decodeList<Event>()
            } catch (_: Exception) {
                events = listOf(Event(id = "1", title = "Annual Cultural Fest", eventDate = "Aug 28", location = "Main Auditorium"))
            }

            try {
                visitors = SupabaseProvider.db.from("visitors").select().decodeList<Visitor>()
            } catch (_: Exception) {
                visitors = listOf(Visitor(id = "1", visitorName = "Ramesh Kumar", purpose = "Parent Visit", status = "Pending"))
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Warden / Admin Control Panel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface),
                actions = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Student View", tint = AccentBlueLight)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector Row
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkCardSurface,
                contentColor = AccentBlueLight
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Complaints (${complaints.size})", modifier = Modifier.padding(12.dp), color = TextPrimary)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Notices", modifier = Modifier.padding(12.dp), color = TextPrimary)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Events", modifier = Modifier.padding(12.dp), color = TextPrimary)
                }
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                    Text("Mess Menu", modifier = Modifier.padding(12.dp), color = TextPrimary)
                }
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                    Text("Visitors", modifier = Modifier.padding(12.dp), color = TextPrimary)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    when (selectedTab) {
                        0 -> AdminComplaintsList(complaints, onStatusChange = { complaint, newStatus ->
                            scope.launch {
                                try {
                                    SupabaseProvider.db.from("complaints")
                                        .update({ set("status", newStatus) }) {
                                            filter { eq("id", complaint.id) }
                                        }
                                } catch (_: Exception) {}
                                refreshData()
                            }
                        })
                        1 -> AdminNoticesList(notices, onAddNotice = { showNoticeDialog = true }, onDeleteNotice = { id ->
                            scope.launch {
                                try {
                                    SupabaseProvider.db.from("notices").delete { filter { eq("id", id) } }
                                } catch (_: Exception) {}
                                refreshData()
                            }
                        })
                        2 -> AdminEventsList(events, onAddEvent = { showEventDialog = true }, onDeleteEvent = { id ->
                            scope.launch {
                                try {
                                    SupabaseProvider.db.from("events").delete { filter { eq("id", id) } }
                                } catch (_: Exception) {}
                                refreshData()
                            }
                        })
                        3 -> AdminMessList(onUpdateMess = { showMessDialog = true })
                        4 -> AdminVisitorsList(visitors, onApproveReject = { visitor, status ->
                            scope.launch {
                                try {
                                    SupabaseProvider.db.from("visitors")
                                        .update({ set("status", status) }) {
                                            filter { eq("id", visitor.id) }
                                        }
                                } catch (_: Exception) {}
                                refreshData()
                            }
                        })
                    }
                }
            }
        }
    }

    // Notice Dialog
    if (showNoticeDialog) {
        AddNoticeDialog(
            onDismiss = { showNoticeDialog = false },
            onSave = { title, body, priority ->
                scope.launch {
                    try {
                        SupabaseProvider.db.from("notices").insert(Notice(title = title, body = body, priority = priority))
                    } catch (_: Exception) {}
                    showNoticeDialog = false
                    refreshData()
                }
            }
        )
    }

    // Event Dialog
    if (showEventDialog) {
        AddEventDialog(
            onDismiss = { showEventDialog = false },
            onSave = { title, desc, loc, date ->
                scope.launch {
                    try {
                        SupabaseProvider.db.from("events").insert(Event(title = title, description = desc, location = loc, eventDate = date))
                    } catch (_: Exception) {}
                    showEventDialog = false
                    refreshData()
                }
            }
        )
    }

    // Mess Dialog
    if (showMessDialog) {
        UpdateMessDialog(
            onDismiss = { showMessDialog = false },
            onSave = { day, meal, items, cal ->
                scope.launch {
                    try {
                        SupabaseProvider.db.from("mess_menu").upsert(MessMenu(day = day, meal = meal, items = items, calories = cal))
                    } catch (_: Exception) {}
                    showMessDialog = false
                    refreshData()
                }
            }
        )
    }
}

@Composable
fun AdminComplaintsList(complaints: List<Complaint>, onStatusChange: (Complaint, String) -> Unit) {
    if (complaints.isEmpty()) {
        Text("No student complaints logged yet.", color = TextMuted)
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(complaints) { complaint ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(complaint.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Badge(containerColor = when (complaint.status.lowercase()) {
                                "resolved" -> StatusGreen
                                "in progress" -> StatusYellow
                                "assigned" -> AccentBlue
                                else -> StatusRed
                            }) {
                                Text(complaint.status.uppercase(), color = TextPrimary, modifier = Modifier.padding(4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(complaint.description ?: "No details provided.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Update Complaint Status:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = complaint.status == "Assigned",
                                onClick = { onStatusChange(complaint, "Assigned") },
                                label = { Text("Assigned") }
                            )
                            FilterChip(
                                selected = complaint.status == "In Progress",
                                onClick = { onStatusChange(complaint, "In Progress") },
                                label = { Text("In Progress") }
                            )
                            FilterChip(
                                selected = complaint.status == "Resolved",
                                onClick = { onStatusChange(complaint, "Resolved") },
                                label = { Text("Resolved ✅") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNoticesList(notices: List<Notice>, onAddNotice: () -> Unit, onDeleteNotice: (String) -> Unit) {
    Column {
        Button(onClick = onAddNotice, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Post New Announcement / Notice")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notices) { notice ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notice.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(notice.body ?: "", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                        IconButton(onClick = { onDeleteNotice(notice.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminEventsList(events: List<Event>, onAddEvent: () -> Unit, onDeleteEvent: (String) -> Unit) {
    Column {
        Button(onClick = onAddEvent, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Event, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Upcoming Event")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(events) { event ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("${event.eventDate} • ${event.location ?: "Hostel"}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                        IconButton(onClick = { onDeleteEvent(event.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMessList(onUpdateMess: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = onUpdateMess, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Restaurant, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Update Today's Mess Menu")
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Mess Operations & Meals", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Wardens can edit daily meal menus, meal timings, and special fest food schedules for students.", color = TextMuted)
            }
        }
    }
}

@Composable
fun AdminVisitorsList(visitors: List<Visitor>, onApproveReject: (Visitor, String) -> Unit) {
    if (visitors.isEmpty()) {
        Text("No visitor requests pending.", color = TextMuted)
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visitors) { visitor ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Visitor: ${visitor.visitorName}", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Purpose: ${visitor.purpose ?: "Personal"}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApproveReject(visitor, "Approved") },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                            ) {
                                Text("Approve Entry")
                            }
                            OutlinedButton(onClick = { onApproveReject(visitor, "Rejected") }) {
                                Text("Reject", color = StatusRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddNoticeDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Low") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post New Announcement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Body / Details") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = priority == "Low", onClick = { priority = "Low" }, label = { Text("Low") })
                    FilterChip(selected = priority == "Medium", onClick = { priority = "Medium" }, label = { Text("Medium") })
                    FilterChip(selected = priority == "High", onClick = { priority = "High" }, label = { Text("High") })
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onSave(title, body, priority) }) {
                Text("Post Notice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddEventDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("Hostel Ground") }
    var date by remember { mutableStateOf("Sep 10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Upcoming Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Location") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (e.g. Sep 10)") })
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onSave(title, desc, loc, date) }) {
                Text("Create Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UpdateMessDialog(onDismiss: () -> Unit, onSave: (String, String, String, Int) -> Unit) {
    var day by remember { mutableStateOf("2026-09-25") }
    var meal by remember { mutableStateOf("Lunch") }
    var items by remember { mutableStateOf("Paneer Butter Masala, Roti, Rice, Dal Fry, Salad") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Today's Mess Menu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = meal, onValueChange = { meal = it }, label = { Text("Meal (Breakfast/Lunch/Dinner)") })
                OutlinedTextField(value = items, onValueChange = { items = it }, label = { Text("Food Items") })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(day, meal, items, 650) }) {
                Text("Save Menu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
