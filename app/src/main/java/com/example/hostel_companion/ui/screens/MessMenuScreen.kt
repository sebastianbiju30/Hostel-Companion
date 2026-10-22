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
import com.example.hostel_companion.data.models.MessMenu
import com.example.hostel_companion.data.repository.HostelRepository
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessMenuScreen() {
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    // Determine current day
    val todayName = SimpleDateFormat("EEEE", Locale.US).format(Date())
    var selectedDay by remember {
        mutableStateOf(if (todayName in daysOfWeek) todayName else "Monday")
    }

    val allMenuItems by HostelRepository.messMenu.collectAsState()
    val menuItems = allMenuItems.filter { it.day.equals(selectedDay, ignoreCase = true) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        HostelRepository.refreshFromSupabase()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Weekly Mess Menu", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Healthy nutrition scheduled for every day", style = MaterialTheme.typography.bodySmall, color = TextCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontal Day Selector Pills
            Surface(
                color = DarkCardSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daysOfWeek) { day ->
                        val isSelected = selectedDay == day
                        val isToday = day.equals(todayName, ignoreCase = true)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AccentBlue else DarkCardSurfaceVariant,
                            modifier = Modifier.clickable { selectedDay = day }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.take(3).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else TextMuted
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "TODAY",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                                        color = if (isSelected) AccentTeal else AccentAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Menu Cards for the Selected Day
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentBlueLight)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(menuItems) { meal ->
                            MealScheduleCard(meal)
                        }

                        // Mess Hall Timings Notice Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCardSurfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Special dietary needs or guest coupons can be requested directly at the Mess Supervisor desk.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
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

@Composable
fun MealScheduleCard(meal: MessMenu) {
    val mealIcon = when (meal.meal.lowercase()) {
        "breakfast" -> Icons.Default.FreeBreakfast
        "lunch" -> Icons.Default.Restaurant
        "snacks" -> Icons.Default.LocalCafe
        else -> Icons.Default.DinnerDining
    }

    val mealColor = when (meal.meal.lowercase()) {
        "breakfast" -> AccentAmber
        "lunch" -> AccentTeal
        "snacks" -> Color(0xFFFB923C)
        else -> AccentPurple
    }

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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(mealColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(mealIcon, contentDescription = null, tint = mealColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = meal.meal,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = meal.serveTime ?: "Regular Timing",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkCardSurfaceVariant
                ) {
                    Text(
                        text = "🔥 ${meal.calories ?: 650} kcal",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DarkCardSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = meal.items ?: "Menu to be updated by warden",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = TextPrimary
            )
        }
    }
}
