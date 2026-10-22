package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.*
import com.example.hostel_companion.data.repository.HostelRepository
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    // 0: Complaints, 1: Room Allocation, 2: Mess Menu, 3: Visitors, 4: Events, 5: Laundry, 6: Parcels
    var selectedTab by remember { mutableIntStateOf(0) }
    val complaints by HostelRepository.complaints.collectAsState()
    val profilesList by HostelRepository.profiles.collectAsState()
    val roomsList by HostelRepository.rooms.collectAsState()
    val messMenuItems by HostelRepository.messMenu.collectAsState()
    val visitors by HostelRepository.visitors.collectAsState()
    val events by HostelRepository.events.collectAsState()
    val laundryOrders by HostelRepository.laundryOrders.collectAsState()
    val parcels by HostelRepository.parcels.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    // Dialog States
    var showRoomAllocDialog by remember { mutableStateOf(false) }
    var selectedStudentForRoom by remember { mutableStateOf<UserProfile?>(null) }
    var showEditMessDialog by remember { mutableStateOf(false) }
    var selectedMessToEdit by remember { mutableStateOf<MessMenu?>(null) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAddParcelDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun refreshAllData() {
        HostelRepository.refreshFromSupabase()
    }

    LaunchedEffect(Unit) {
        refreshAllData()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Warden Administration Portal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        }
                        Text("Chief Warden Control • Full System Access", style = MaterialTheme.typography.labelSmall, color = AccentAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface),
                actions = {
                    IconButton(onClick = { refreshAllData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data", tint = TextPrimary)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            try { SupabaseProvider.auth.signOut() } catch (_: Exception) {}
                            navController.navigate("auth") { popUpTo(0) { inclusive = true } }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out", tint = StatusRed)
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
            // Tabs Bar for Warden
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkCardSurface,
                contentColor = AccentAmber
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Complaints (${complaints.size})", modifier = Modifier.padding(12.dp), color = if (selectedTab == 0) AccentAmber else TextMuted, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Room Allocation", modifier = Modifier.padding(12.dp), color = if (selectedTab == 1) AccentAmber else TextMuted, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Mess Menu", modifier = Modifier.padding(12.dp), color = if (selectedTab == 2) AccentAmber else TextMuted, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                    Text("Visitor Passes (${visitors.filter { it.status.lowercase() == "pending" }.size})", modifier = Modifier.padding(12.dp), color = if (selectedTab == 3) AccentAmber else TextMuted, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                    Text("Events (${events.size})", modifier = Modifier.padding(12.dp), color = if (selectedTab == 4) AccentAmber else TextMuted, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }) {
                    Text("Laundry", modifier = Modifier.padding(12.dp), color = if (selectedTab == 5) AccentAmber else TextMuted, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 6, onClick = { selectedTab = 6 }) {
                    Text("Parcels", modifier = Modifier.padding(12.dp), color = if (selectedTab == 6) AccentAmber else TextMuted, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentAmber)
                } else {
                    when (selectedTab) {
                        // 1. COMPLAINTS TAB
                        0 -> WardenComplaintsView(
                            complaints = complaints,
                            onImageClick = { previewImageUrl = it },
                            onStatusUpdate = { complaint, newStatus ->
                                HostelRepository.updateComplaintStatus(complaint.id, newStatus)
                            }
                        )

                        // 2. ROOM ALLOCATION TAB
                        1 -> WardenRoomAllocationView(
                            profiles = profilesList,
                            rooms = roomsList,
                            onAllocateClick = { student ->
                                selectedStudentForRoom = student
                                showRoomAllocDialog = true
                            }
                        )

                        // 3. MESS MENU TAB
                        2 -> WardenMessMenuView(
                            menuItems = messMenuItems,
                            onEditMeal = { meal ->
                                selectedMessToEdit = meal
                                showEditMessDialog = true
                            }
                        )

                        // 4. VISITOR PASSES TAB
                        3 -> WardenVisitorsView(
                            visitors = visitors,
                            onApprove = { visitor ->
                                HostelRepository.updateVisitorStatus(visitor.id, "Approved")
                            },
                            onReject = { visitor ->
                                HostelRepository.updateVisitorStatus(visitor.id, "Rejected")
                            }
                        )

                        // 5. EVENTS TAB
                        4 -> WardenEventsView(
                            events = events,
                            onAddEventClick = { showAddEventDialog = true },
                            onDeleteEvent = { eventId ->
                                HostelRepository.deleteEvent(eventId)
                            }
                        )

                        // 6. LAUNDRY TAB
                        5 -> WardenLaundryView(
                            orders = laundryOrders,
                            onUpdateStatus = { order, newStatus ->
                                HostelRepository.updateLaundryStatus(order.id, newStatus)
                            }
                        )

                        // 7. PARCELS TAB
                        6 -> WardenParcelsView(
                            parcels = parcels,
                            onAddParcelClick = { showAddParcelDialog = true },
                            onUpdateStatus = { parcel, newStatus ->
                                HostelRepository.updateParcelStatus(parcel.id, newStatus)
                            }
                        )
                    }
                }
            }
        }

        // Image Zoom Preview
        previewImageUrl?.let { imgUrl ->
            Dialog(onDismissRequest = { previewImageUrl = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Complaint Photo Preview", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            IconButton(onClick = { previewImageUrl = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                        }
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // Room Allocation Dialog
        if (showRoomAllocDialog && selectedStudentForRoom != null) {
            var inputRoomNumber by remember { mutableStateOf("A-304") }
            var inputBlock by remember { mutableStateOf("North Block") }

            AlertDialog(
                onDismissRequest = { showRoomAllocDialog = false },
                title = { Text("Allocate Room for ${selectedStudentForRoom?.fullName ?: "Student"}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Assign room number and block for this resident:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        OutlinedTextField(
                            value = inputRoomNumber,
                            onValueChange = { inputRoomNumber = it },
                            label = { Text("Room Number (e.g. A-304, B-201)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputBlock,
                            onValueChange = { inputBlock = it },
                            label = { Text("Hostel Block (North / South / East)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val student = selectedStudentForRoom!!
                            showRoomAllocDialog = false
                            if (inputRoomNumber.isNotBlank()) {
                                HostelRepository.allocateRoom(
                                    studentId = student.id,
                                    roomNumber = inputRoomNumber.trim(),
                                    block = inputBlock.trim()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                    ) {
                        Text("Confirm Allocation", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRoomAllocDialog = false }) { Text("Cancel", color = TextMuted) }
                },
                containerColor = DarkCardSurface
            )
        }

        // Edit Mess Menu Dialog
        if (showEditMessDialog && selectedMessToEdit != null) {
            var itemsText by remember { mutableStateOf(selectedMessToEdit?.items ?: "") }
            var timingText by remember { mutableStateOf(selectedMessToEdit?.serveTime ?: "") }
            var caloriesText by remember { mutableStateOf(selectedMessToEdit?.calories?.toString() ?: "650") }

            AlertDialog(
                onDismissRequest = { showEditMessDialog = false },
                title = { Text("Update ${selectedMessToEdit?.meal} (${selectedMessToEdit?.day})") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = itemsText,
                            onValueChange = { itemsText = it },
                            label = { Text("Menu Items (comma separated)") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = timingText,
                            onValueChange = { timingText = it },
                            label = { Text("Serving Hours (e.g. 12:30 PM - 02:30 PM)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = caloriesText,
                            onValueChange = { caloriesText = it },
                            label = { Text("Approx Calories (kcal)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val meal = selectedMessToEdit!!
                            showEditMessDialog = false
                            HostelRepository.updateMessMenuItem(
                                mealId = meal.id,
                                items = itemsText.trim(),
                                serveTime = timingText.trim(),
                                calories = caloriesText.toIntOrNull() ?: 650
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                    ) {
                        Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditMessDialog = false }) { Text("Cancel", color = TextMuted) }
                },
                containerColor = DarkCardSurface
            )
        }

        // Add Event Dialog
        if (showAddEventDialog) {
            var eventTitle by remember { mutableStateOf("") }
            var eventDate by remember { mutableStateOf("") }
            var eventLocation by remember { mutableStateOf("") }
            var eventDesc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddEventDialog = false },
                title = { Text("Post New Hostel Event") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = eventTitle, onValueChange = { eventTitle = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = eventDate, onValueChange = { eventDate = it }, label = { Text("Date & Time (e.g. Nov 14 • 07:00 PM)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = eventLocation, onValueChange = { eventLocation = it }, label = { Text("Venue / Location") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = eventDesc, onValueChange = { eventDesc = it }, label = { Text("Description & Highlights") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (eventTitle.isNotBlank()) {
                                showAddEventDialog = false
                                val newEv = Event(
                                    id = UUID.randomUUID().toString(),
                                    title = eventTitle.trim(),
                                    eventDate = eventDate.trim(),
                                    location = eventLocation.trim(),
                                    description = eventDesc.trim()
                                )
                                HostelRepository.addEvent(newEv)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                    ) {
                        Text("Publish Event", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEventDialog = false }) { Text("Cancel", color = TextMuted) }
                },
                containerColor = DarkCardSurface
            )
        }

        // Log Parcel Dialog
        if (showAddParcelDialog) {
            var parcelSender by remember { mutableStateOf("") }
            var parcelDesc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddParcelDialog = false },
                title = { Text("Log Received Parcel at Gate") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = parcelSender, onValueChange = { parcelSender = it }, label = { Text("Sender / Courier (e.g. Amazon, Flipkart)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = parcelDesc, onValueChange = { parcelDesc = it }, label = { Text("Description & Student Name / Room") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (parcelSender.isNotBlank()) {
                                showAddParcelDialog = false
                                val newP = Parcel(
                                    id = UUID.randomUUID().toString(),
                                    sender = parcelSender.trim(),
                                    description = parcelDesc.trim(),
                                    status = "Ready for Pickup",
                                    arrivedAt = SimpleDateFormat("hh:mm a, dd MMM", Locale.US).format(Date())
                                )
                                HostelRepository.addParcel(newP)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                    ) {
                        Text("Log Parcel", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddParcelDialog = false }) { Text("Cancel", color = TextMuted) }
                },
                containerColor = DarkCardSurface
            )
        }
    }
}

// ----------------- SUB-VIEWS FOR EACH TAB -----------------

@Composable
fun WardenComplaintsView(
    complaints: List<Complaint>,
    onImageClick: (String) -> Unit,
    onStatusUpdate: (Complaint, String) -> Unit
) {
    if (complaints.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No complaints logged yet.", color = TextMuted)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(complaints) { complaint ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(complaint.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text("Category: ${complaint.category}", style = MaterialTheme.typography.bodySmall, color = AccentTeal)
                            }
                            StatusBadge(status = complaint.status)
                        }

                        complaint.description?.let { desc ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(desc, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }

                        // Photo Thumbnail if present
                        complaint.photoUrl?.let { url ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onImageClick(url) },
                                color = DarkCardSurfaceVariant
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                                    ) {
                                        Text("Tap to Zoom", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkCardSurfaceVariant)

                        Text("Update Resolution Status:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Assigned", "In Progress", "Resolved").forEach { st ->
                                val isCurrent = complaint.status.equals(st, true)
                                OutlinedButton(
                                    onClick = { onStatusUpdate(complaint, st) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isCurrent) AccentAmber.copy(alpha = 0.2f) else Color.Transparent,
                                        contentColor = if (isCurrent) AccentAmber else TextMuted
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(if (isCurrent) AccentAmber else DarkCardSurfaceVariant)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text(if (st == "Resolved") "Resolved ✓" else st, style = MaterialTheme.typography.labelSmall, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardenRoomAllocationView(
    profiles: List<UserProfile>,
    rooms: List<Room>,
    onAllocateClick: (UserProfile) -> Unit
) {
    Column {
        Text("Student Residents & Room Allotments", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Text("Click on any resident to assign or change their room number", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(profiles) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName ?: "Student", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text(student.email ?: "", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = if (student.roomId != null) AccentBlue.copy(alpha = 0.2f) else StatusRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (student.roomId != null) "Allotted: Room ${student.roomId}" else "No Room Allotted",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (student.roomId != null) AccentBlueLight else StatusRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { onAllocateClick(student) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Allocate / Change", color = Color.Black, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardenMessMenuView(
    menuItems: List<MessMenu>,
    onEditMeal: (MessMenu) -> Unit
) {
    Column {
        Text("Hostel Mess Menu Schedule", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Text("Warden can update menu items, serving hours, and calories for any meal", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(menuItems) { meal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${meal.day} • ${meal.meal}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = AccentAmber)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(meal.serveTime ?: "", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(meal.items ?: "Menu to be filled", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("🔥 ${meal.calories ?: 650} kcal", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                        }

                        Button(
                            onClick = { onEditMeal(meal) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCardSurfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardenVisitorsView(
    visitors: List<Visitor>,
    onApprove: (Visitor) -> Unit,
    onReject: (Visitor) -> Unit
) {
    Column {
        Text("Student Visitor Pass Requests", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Text("Approve or reject parent & guest entry permissions", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Spacer(modifier = Modifier.height(14.dp))

        if (visitors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No visitor requests recorded.", color = TextMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(visitors) { v ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(v.visitorName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                    Text("Phone: ${v.visitorPhone ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text("Purpose: ${v.purpose ?: "Hostel Visit"}", style = MaterialTheme.typography.bodySmall, color = TextCyan)
                                }

                                Surface(
                                    color = when (v.status.lowercase()) {
                                        "approved" -> StatusGreen.copy(alpha = 0.2f)
                                        "rejected" -> StatusRed.copy(alpha = 0.2f)
                                        else -> AccentAmber.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = v.status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = when (v.status.lowercase()) {
                                            "approved" -> StatusGreen
                                            "rejected" -> StatusRed
                                            else -> AccentAmber
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (v.status.lowercase() == "pending") {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkCardSurfaceVariant)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onApprove(v) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve Pass", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Button(
                                        onClick = { onReject(v) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
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

@Composable
fun WardenEventsView(
    events: List<Event>,
    onAddEventClick: () -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Campus & Hostel Events", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text("Manage tournaments, fests, and workshops", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Button(onClick = onAddEventClick, colors = ButtonDefaults.buttonColors(containerColor = AccentAmber), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Event", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(events) { ev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ev.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text("📅 ${ev.eventDate} • 📍 ${ev.location ?: "Hostel Grounds"}", style = MaterialTheme.typography.bodySmall, color = AccentTeal)
                            ev.description?.let { desc ->
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }

                        IconButton(onClick = { onDeleteEvent(ev.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = StatusRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardenLaundryView(
    orders: List<LaundryOrder>,
    onUpdateStatus: (LaundryOrder, String) -> Unit
) {
    Column {
        Text("Hostel Laundry Service Desk", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
        Text("Update washing, drying, and pickup readiness", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(orders) { o ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("${o.itemsCount} Clothes Dropped", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text("Dropped: ${o.droppedAt ?: "Recently"}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }

                            Surface(
                                color = when (o.status.lowercase()) {
                                    "ready", "collected" -> StatusGreen.copy(alpha = 0.2f)
                                    "washing" -> AccentBlue.copy(alpha = 0.2f)
                                    "drying" -> AccentAmber.copy(alpha = 0.2f)
                                    else -> DarkCardSurfaceVariant
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = o.status.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (o.status.lowercase()) {
                                        "ready", "collected" -> StatusGreen
                                        "washing" -> AccentBlueLight
                                        "drying" -> AccentAmber
                                        else -> TextMuted
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkCardSurfaceVariant)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Washing", "Drying", "Ready", "Collected").forEach { st ->
                                val isCur = o.status.equals(st, true)
                                OutlinedButton(
                                    onClick = { onUpdateStatus(o, st) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isCur) AccentAmber.copy(alpha = 0.2f) else Color.Transparent,
                                        contentColor = if (isCur) AccentAmber else TextMuted
                                    ),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                                ) {
                                    Text(st, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardenParcelsView(
    parcels: List<Parcel>,
    onAddParcelClick: () -> Unit,
    onUpdateStatus: (Parcel, String) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Security Gate Parcel Desk", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text("Log incoming courier packages and mark deliveries", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Button(onClick = onAddParcelClick, colors = ButtonDefaults.buttonColors(containerColor = AccentAmber), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Parcel", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(parcels) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.sender ?: "Courier Parcel", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text(p.description ?: "Package", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                p.arrivedAt?.let { arr ->
                                    Text("Arrived: $arr", style = MaterialTheme.typography.labelSmall, color = TextCyan)
                                }
                            }

                            Surface(
                                color = when (p.status.lowercase()) {
                                    "ready for pickup", "collected" -> StatusGreen.copy(alpha = 0.2f)
                                    else -> AccentAmber.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = p.status.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (p.status.lowercase()) {
                                        "ready for pickup", "collected" -> StatusGreen
                                        else -> AccentAmber
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkCardSurfaceVariant)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Arrived", "Ready for Pickup", "Collected").forEach { st ->
                                val isCur = p.status.equals(st, true)
                                OutlinedButton(
                                    onClick = { onUpdateStatus(p, st) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isCur) AccentAmber.copy(alpha = 0.2f) else Color.Transparent,
                                        contentColor = if (isCur) AccentAmber else TextMuted
                                    ),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                                ) {
                                    Text(st, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
