package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.ui.theme.*

@Composable
fun DashboardScreen(navController: NavController) {
    val user = SupabaseProvider.auth.currentUserOrNull()
    val userName = user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Sebastian"

    Scaffold(
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
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkCardSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Search & Notification Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                onClick = { },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(DarkCardSurface)
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
                            }
                            // Red Dot Badge
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

            // 3. STATS ROW (ROOM, MESS BALANCE, ATTENDANCE)
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        StatCard(
                            title = "A-304",
                            subtitle = "Room",
                            icon = Icons.Default.Domain,
                            iconBg = Color(0xFF1E293B),
                            iconTint = Color(0xFF38BDF8)
                        )
                    }
                    item {
                        StatCard(
                            title = "₹2,450",
                            subtitle = "Mess Balance",
                            icon = Icons.Default.CreditCard,
                            iconBg = Color(0xFF1E293B),
                            iconTint = Color(0xFF34D399)
                        )
                    }
                    item {
                        StatCard(
                            title = "96%",
                            subtitle = "Attendance",
                            icon = Icons.Default.Timeline,
                            iconBg = Color(0xFF1E293B),
                            iconTint = AccentAmber
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
                        QuickActionButton("Room", Icons.Default.Domain, Color(0xFF1E293B), Color(0xFF38BDF8)) { }
                        QuickActionButton("Mess Menu", Icons.Default.FreeBreakfast, Color(0xFF1E293B), AccentAmber) { }
                        QuickActionButton("Complaints", Icons.Default.Warning, Color(0xFF1E293B), AccentRed) { navController.navigate("complaints") }
                        QuickActionButton("Notices", Icons.Default.Assignment, Color(0xFF1E293B), AccentTeal) { navController.navigate("notices") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionButton("Events", Icons.Default.DateRange, Color(0xFF1E293B), AccentPurple) { navController.navigate("events") }
                        QuickActionButton("Laundry", Icons.Default.Checkroom, Color(0xFF1E293B), Color(0xFF06B6D4)) { }
                        QuickActionButton("Parcels", Icons.Default.Inventory2, Color(0xFF1E293B), Color(0xFF6366F1)) { }
                        QuickActionButton("More", Icons.Default.MoreHoriz, Color(0xFF1E293B), TextMuted) { }
                    }
                }
            }

            // 5. ACTIVE COMPLAINT STEPPER CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                    text = "AC Not Cooling (Ref #204)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Surface(
                                color = Color(0xFF451A03),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "IN PROGRESS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AccentAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stepper Line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StepItem("SUBMITTED", isCompleted = true, isActive = false)
                            StepItem("ASSIGNED", isCompleted = true, isActive = false)
                            StepItem("IN PROGRESS", isCompleted = false, isActive = true)
                            StepItem("RESOLVED", isCompleted = false, isActive = false)
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
                            modifier = Modifier.clickable { }
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                Text("John Doe • Approved", style = MaterialTheme.typography.bodySmall, color = TextMuted)
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

                    NoticeItemCard("HIGH", AccentRed, "Water Supply Maintenance", "2h ago")
                    NoticeItemCard("MEDIUM", AccentAmber, "Room Inspection Schedule", "1d ago")
                    NoticeItemCard("LOW", AccentBlueLight, "Hostel Fee Deadline", "2d ago")
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
                        EmergencyCard("Warden", "North Block", Icons.Default.Phone, modifier = Modifier.weight(1f))
                        EmergencyCard("Medical", "Health Center", Icons.Default.Add, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EmergencyCard("Security", "Main Gate", Icons.Default.Warning, modifier = Modifier.weight(1f))
                        EmergencyCard("Office", "Helpdesk", Icons.Default.Business, modifier = Modifier.weight(1f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
fun StatCard(title: String, subtitle: String, icon: ImageVector, iconBg: Color, iconTint: Color) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(110.dp),
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
fun NoticeItemCard(priority: String, badgeColor: Color, title: String, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
fun EmergencyCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
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
