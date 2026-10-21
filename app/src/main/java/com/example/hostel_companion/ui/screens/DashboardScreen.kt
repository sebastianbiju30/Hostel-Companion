package com.example.hostel_companion.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.*
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val user = SupabaseProvider.auth.currentUserOrNull()
    val userName = user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Sebastian"

    var roomNumber by remember { mutableStateOf("A-304") }
    var messBalance by remember { mutableStateOf("₹2,450") }
    var activeComplaint by remember { mutableStateOf<Complaint?>(null) }
    var latestNotice by remember { mutableStateOf<Notice?>(null) }
    var latestVisitor by remember { mutableStateOf<Visitor?>(null) }
    var emergencyContacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var isUserAdmin by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val userId = user?.id ?: ""
                val profile = SupabaseProvider.db.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<UserProfile>()
                if (profile?.isAdmin == true) {
                    isUserAdmin = true
                }
                if (profile?.roomId != null) {
                    val room = SupabaseProvider.db.from("rooms")
                        .select { filter { eq("id", profile.roomId) } }
                        .decodeSingleOrNull<Room>()
                    if (room != null) roomNumber = room.roomNumber
                }

                val complaints = SupabaseProvider.db.from("complaints")
                    .select { filter { eq("student_id", userId) } }
                    .decodeList<Complaint>()
                activeComplaint = complaints.firstOrNull { it.status.lowercase() != "resolved" } ?: complaints.firstOrNull()

                val notices = SupabaseProvider.db.from("notices")
                    .select()
                    .decodeList<Notice>()
                latestNotice = notices.firstOrNull()

                val visitors = SupabaseProvider.db.from("visitors")
                    .select { filter { eq("student_id", userId) } }
                    .decodeList<Visitor>()
                latestVisitor = visitors.firstOrNull()

                emergencyContacts = SupabaseProvider.db.from("emergency_contacts")
                    .select()
                    .decodeList<EmergencyContact>()
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("complaints") },
                containerColor = AccentBlueLight,
                contentColor = TextPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Complaint", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. TOP HEADER & GREETING
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkCardSurfaceVariant)
                            .clickable { navController.navigate("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Avatar", tint = TextPrimary, modifier = Modifier.size(26.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = { navController.navigate("chatbot") },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkCardSurface)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = AccentTeal)
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkCardSurface)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                        }
                        Box {
                            IconButton(
                                onClick = { navController.navigate("notices") },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(DarkCardSurface)
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(AccentRed)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }

            // 2. GREETING TEXT
            item {
                Column {
                    Text(
                        text = "Friday, August 28",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Good Morning, $userName 👋",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }

            // 2b. WARDEN CONTROL PANEL BANNER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("admin_dashboard") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isUserAdmin) "👑 Warden Control Center" else "🛡️ Open Warden / Admin Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Resolve complaints, post notices, mess menu & events.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.SupervisorAccount,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 2c. AI CAMPUS ASSISTANT BANNER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("chatbot") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(AccentBlueLight, AccentPurple)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(AccentBlueLight, AccentPurple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ask Campus AI Assistant",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = AccentTeal.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "AI",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AccentTeal,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Instant answers for mess menu, complaints, laundry, rules & contacts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                }
            }

            // 3. STATS ROW (ROOM, MESS BALANCE, ATTENDANCE)
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        StatCard(
                            title = roomNumber,
                            subtitle = "Room",
                            icon = Icons.Default.Domain,
                            iconBg = Color(0xFF1E293B),
                            iconTint = Color(0xFF38BDF8),
                            onClick = { navController.navigate("room") }
                        )
                    }
                    item {
                        StatCard(
                            title = messBalance,
                            subtitle = "Mess Balance",
                            icon = Icons.Default.CreditCard,
                            iconBg = Color(0xFF1E293B),
                            iconTint = Color(0xFF34D399),
                            onClick = { navController.navigate("mess") }
                        )
                    }
                    item {
                        StatCard(
                            title = "96%",
                            subtitle = "Attendance",
                            icon = Icons.Default.Timeline,
                            iconBg = Color(0xFF1E293B),
                            iconTint = AccentAmber,
                            onClick = { }
                        )
                    }
                }
            }

            // 4. QUICK ACTIONS GRID
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionButton("Room", Icons.Default.Domain, Color(0xFF1E293B), Color(0xFF38BDF8)) { navController.navigate("room") }
                        QuickActionButton("Mess Menu", Icons.Default.FreeBreakfast, Color(0xFF1E293B), AccentAmber) { navController.navigate("mess") }
                        QuickActionButton("Complaints", Icons.Default.Warning, Color(0xFF1E293B), AccentRed) { navController.navigate("complaints") }
                        QuickActionButton("Notices", Icons.AutoMirrored.Filled.Assignment, Color(0xFF1E293B), AccentTeal) { navController.navigate("notices") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionButton("Events", Icons.Default.DateRange, Color(0xFF1E293B), AccentPurple) { navController.navigate("events") }
                        QuickActionButton("Laundry", Icons.Default.Checkroom, Color(0xFF1E293B), Color(0xFF06B6D4)) { navController.navigate("laundry") }
                        QuickActionButton("Parcels", Icons.Default.Inventory2, Color(0xFF1E293B), Color(0xFF6366F1)) { navController.navigate("parcels") }
                        QuickActionButton("Visitors", Icons.Default.Group, Color(0xFF1E293B), Color(0xFF10B981)) { navController.navigate("visitors") }
                    }
                }
            }

            // 5. ACTIVE COMPLAINT STEPPER CARD
            item {
                val complaintTitle = activeComplaint?.title ?: "AC Not Cooling (Ref #204)"
                val complaintStatus = activeComplaint?.status ?: "In Progress"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("complaints") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Active Complaint",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = complaintTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Surface(
                                color = Color(0xFF451A03),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = complaintStatus.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AccentAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val statusLower = complaintStatus.lowercase()
                            StepItem("SUBMITTED", isCompleted = true, isActive = false)
                            StepItem("ASSIGNED", isCompleted = statusLower in listOf("assigned", "in progress", "resolved"), isActive = false)
                            StepItem("IN PROGRESS", isCompleted = statusLower == "resolved", isActive = statusLower == "in progress")
                            StepItem("RESOLVED", isCompleted = false, isActive = statusLower == "resolved")
                        }
                    }
                }
            }

            // 6. TODAY'S MESS MENU
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Mess Menu",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Full Menu >",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextCyan,
                            modifier = Modifier.clickable { navController.navigate("mess") }
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("mess") },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            MessMealItem("Breakfast", "Poha, Boiled Eggs, Tea", "07:30 AM", "450 kcal", isCurrent = false)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkCardSurfaceVariant)
                            MessMealItem("Lunch", "Rice, Dal, Paneer Masala", "12:30 PM", "850 kcal", isCurrent = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkCardSurfaceVariant)
                            MessMealItem("Snacks", "Samosa, Coffee", "05:00 PM", "320 kcal", isCurrent = false)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkCardSurfaceVariant)
                            MessMealItem("Dinner", "Chapati, Veg Curry", "08:00 PM", "650 kcal", isCurrent = false)
                        }
                    }
                }
            }

            // 7. VISITOR REQUEST CARD
            item {
                val visitorName = latestVisitor?.visitorName ?: "John Doe"
                val visitorStatus = latestVisitor?.status ?: "Approved"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("visitors") },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF064E3B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, tint = AccentTeal)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Visitor Request", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("$visitorName • $visitorStatus", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                        Surface(
                            color = Color(0xFF064E3B),
                            shape = CircleShape
                        ) {
                            Text(
                                "Pass",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentTeal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 8. LATEST NOTICES
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Latest Notices",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "View All >",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextCyan,
                            modifier = Modifier.clickable { navController.navigate("notices") }
                        )
                    }

                    NoticeItemCard("HIGH", AccentRed, latestNotice?.title ?: "Water Supply Maintenance", "2h ago") { navController.navigate("notices") }
                    NoticeItemCard("MEDIUM", AccentAmber, "Room Inspection Schedule", "1d ago") { navController.navigate("notices") }
                    NoticeItemCard("LOW", AccentBlueLight, "Hostel Fee Deadline", "2d ago") { navController.navigate("notices") }
                }
            }

            // 9. UPCOMING EVENTS BANNER
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Upcoming Events",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(Color(0xFF4C1D95))
                            ) {
                                Surface(
                                    color = Color(0xFF1E1B4B),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Text(
                                        "Aug 24",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Annual Cultural Fest", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Main Auditorium", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { navController.navigate("events") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkCardSurfaceVariant)
                                ) {
                                    Text("Register Now", color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // 10. EMERGENCY CONTACTS GRID
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Emergency",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EmergencyCard("Warden", "North Block", "+91-9000000001", Icons.Default.Phone, modifier = Modifier.weight(1f)) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919000000001"))
                            context.startActivity(intent)
                        }
                        EmergencyCard("Medical", "Health Center", "+91-9000000002", Icons.Default.Add, modifier = Modifier.weight(1f)) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919000000002"))
                            context.startActivity(intent)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EmergencyCard("Security", "Main Gate", "+91-9000000003", Icons.Default.Warning, modifier = Modifier.weight(1f)) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919000000003"))
                            context.startActivity(intent)
                        }
                        EmergencyCard("Office", "Helpdesk", "+91-9000000004", Icons.Default.Business, modifier = Modifier.weight(1f)) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919000000004"))
                            context.startActivity(intent)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun StatCard(title: String, subtitle: String, icon: ImageVector, iconBg: Color, iconTint: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun QuickActionButton(label: String, icon: ImageVector, bg: Color, iconTint: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontSize = 11.sp)
    }
}

@Composable
fun StepItem(label: String, isCompleted: Boolean, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted || isActive -> AccentBlueLight
                        else -> DarkCardSurfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(12.dp))
            } else if (isActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TextPrimary)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isCompleted || isActive) AccentBlueLight else TextMuted,
            fontSize = 9.sp
        )
    }
}

@Composable
fun MessMealItem(title: String, menu: String, time: String, calories: String, isCurrent: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) AccentBlueLight else TextMuted)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = if (isCurrent) AccentBlueLight else TextPrimary)
                Text(menu, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Spacer(modifier = Modifier.height(2.dp))
                Text("🔥 $calories", style = MaterialTheme.typography.labelSmall, color = AccentAmber)
            }
        }
        Text(time, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@Composable
fun NoticeItemCard(priority: String, badgeColor: Color, title: String, time: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = badgeColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    priority,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(time, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun EmergencyCard(title: String, subtitle: String, phone: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EmergencyCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(EmergencyIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    }
}
