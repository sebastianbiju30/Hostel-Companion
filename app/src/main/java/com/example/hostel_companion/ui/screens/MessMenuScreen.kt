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

    var menuItems by remember { mutableStateOf<List<MessMenu>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // 7-Day comprehensive fallback menu
    val weeklyFullMenu = remember {
        mapOf(
            "Monday" to listOf(
                MessMenu(id = "m1", day = "Monday", meal = "Breakfast", items = "Aloo Paratha with Curd, Boiled Eggs / Banana, Butter & Jam, Masala Tea", calories = 520, serveTime = "07:30 AM - 09:30 AM"),
                MessMenu(id = "m2", day = "Monday", meal = "Lunch", items = "Rajma Masala, Steamed Jeera Rice, Phulka, Boondi Raita, Fresh Green Salad", calories = 740, serveTime = "12:30 PM - 02:30 PM"),
                MessMenu(id = "m3", day = "Monday", meal = "Snacks", items = "Crispy Veg Cutlet with Mint Dip, Filter Coffee / Tea", calories = 310, serveTime = "05:00 PM - 06:00 PM"),
                MessMenu(id = "m4", day = "Monday", meal = "Dinner", items = "Kadai Paneer, Yellow Dal Fry, Tawa Roti, Rice, Fruit Custard", calories = 680, serveTime = "07:30 PM - 09:30 PM")
            ),
            "Tuesday" to listOf(
                MessMenu(id = "t1", day = "Tuesday", meal = "Breakfast", items = "Masala Dosa with Coconut & Tomato Chutney, Sambar, Filter Coffee", calories = 480, serveTime = "07:30 AM - 09:30 AM"),
                MessMenu(id = "t2", day = "Tuesday", meal = "Lunch", items = "Chole Bhature / Kulche, Pulao, Curd, Pickled Onions", calories = 820, serveTime = "12:30 PM - 02:30 PM"),
                MessMenu(id = "t3", day = "Tuesday", meal = "Snacks", items = "Bhel Puri / Sev Puri, Hot Masala Chai", calories = 260, serveTime = "05:00 PM - 06:00 PM"),
                MessMenu(id = "t4", day = "Tuesday", meal = "Dinner", items = "Palak Paneer, Moong Dal Khichdi, Phulka, Papad, Gulab Jamun", calories = 660, serveTime = "07:30 PM - 09:30 PM")
            ),
            "Wednesday" to listOf(
                MessMenu(id = "w1", day = "Wednesday", meal = "Breakfast", items = "Idli & Vada with Sambar, Chutney, Bread Omelette / Sprouts, Tea", calories = 490, serveTime = "07:30 AM - 09:30 AM"),
                MessMenu(id = "w2", day = "Wednesday", meal = "Lunch", items = "Paneer Butter Masala, Dal Tadka, Ghee Rice, Roti, Cucumber Salad", calories = 760, serveTime = "12:30 PM - 02:30 PM"),
                MessMenu(id = "w3", day = "Wednesday", meal = "Snacks", items = "Hot Onion Pakoda, Green Chutney, Ginger Chai", calories = 340, serveTime = "05:00 PM - 06:00 PM"),
                MessMenu(id = "w4", day = "Wednesday", meal = "Dinner", items = "Egg Curry / Malai Kofta, Dal Makhani, Phulka, Jeera Rice, Ice Cream", calories = 710, serveTime = "07:30 PM - 09:30 PM")
            ),
            "Thursday" to listOf(
                MessMenu(id = "th1", day = "Thursday", meal = "Breakfast", items = "Poha with Sev & Peanuts, Boiled Eggs / Fruit Bowl, Tea / Coffee", calories = 460, serveTime = "07:30 AM - 09:30 AM"),
                MessMenu(id = "th2", day = "Thursday", meal = "Lunch", items = "Kadhi Pakoda, Aloo Jeera, Steamed Basmati Rice, Chapati, Salad", calories = 690, serveTime = "12:30 PM - 02:30 PM"),
                MessMenu(id = "th3", day = "Thursday", meal = "Snacks", items = "Samosa with Sweet Tamarind Chutney, Tea", calories = 330, serveTime = "05:00 PM - 06:00 PM"),
                MessMenu(id = "th4", day = "Thursday", meal = "Dinner", items = "Shahi Veg Biryani, Mirchi Ka Salan, Veg Raita, Sweet Kheer", calories = 750, serveTime = "07:30 PM - 09:30 PM")
            ),
            "Friday" to listOf(
                MessMenu(id = "f1", day = "Friday", meal = "Breakfast", items = "Uttapam with Sambar, Coconut Chutney, Scrambled Eggs / Sprouted Moong", calories = 470, serveTime = "07:30 AM - 09:30 AM"),
                MessMenu(id = "f2", day = "Friday", meal = "Lunch", items = "Dal Palak, Mix Veg Dry, Steamed Rice, Tawa Roti, Curd", calories = 670, serveTime = "12:30 PM - 02:30 PM"),
                MessMenu(id = "f3", day = "Friday", meal = "Snacks", items = "Grilled Veg Sandwich, Hot Bournvita / Tea", calories = 290, serveTime = "05:00 PM - 06:00 PM"),
                MessMenu(id = "f4", day = "Friday", meal = "Dinner", items = "Matar Paneer, Dal Fry, Phulka, Rice, Rasgulla", calories = 690, serveTime = "07:30 PM - 09:30 PM")
            ),
            "Saturday" to listOf(
                MessMenu(id = "s1", day = "Saturday", meal = "Breakfast", items = "Poori Bhaji (Aloo Tamatar), Boiled Eggs, Halwa, Masala Chai", calories = 580, serveTime = "08:00 AM - 10:00 AM"),
                MessMenu(id = "s2", day = "Saturday", meal = "Lunch", items = "Lemon Rice, Sambar, Potato Roast, Curd Rice, Fryums", calories = 720, serveTime = "12:30 PM - 02:30 PM"),
                MessMenu(id = "s3", day = "Saturday", meal = "Snacks", items = "Pav Bhaji (2 Butter Pav), Lemonade / Tea", calories = 380, serveTime = "05:00 PM - 06:00 PM"),
                MessMenu(id = "s4", day = "Saturday", meal = "Dinner", items = "Veg Manchurian with Fried Rice / Noodles, Sweet Corn Soup", calories = 730, serveTime = "07:30 PM - 09:30 PM")
            ),
            "Sunday" to listOf(
                MessMenu(id = "su1", day = "Sunday", meal = "Breakfast", items = "Executive Weekend Brunch: Chole Bhature, Dosa, Eggs to order, Fresh Juice", calories = 650, serveTime = "08:00 AM - 10:30 AM"),
                MessMenu(id = "su2", day = "Sunday", meal = "Lunch", items = "Sunday Feast: Chicken Dum Biryani / Shahi Paneer Biryani, Mirchi Salan, Raita, Gulab Jamun", calories = 890, serveTime = "12:30 PM - 02:45 PM"),
                MessMenu(id = "su3", day = "Sunday", meal = "Snacks", items = "French Fries with Cheese Dip, Cold Coffee / Hot Tea", calories = 350, serveTime = "05:00 PM - 06:00 PM"),
                MessMenu(id = "su4", day = "Sunday", meal = "Dinner", items = "Light Dinner: Dal Tadka, Seasonal Sabzi, Steamed Rice, Soft Phulkas, Fresh Cut Fruits", calories = 590, serveTime = "07:30 PM - 09:30 PM")
            )
        )
    }

    LaunchedEffect(selectedDay) {
        scope.launch {
            isLoading = true
            try {
                val dbMeals = SupabaseProvider.db.from("mess_menu")
                    .select { filter { eq("day", selectedDay) } }
                    .decodeList<MessMenu>()
                if (dbMeals.isNotEmpty()) {
                    menuItems = dbMeals
                } else {
                    menuItems = weeklyFullMenu[selectedDay] ?: emptyList()
                }
            } catch (_: Exception) {
                menuItems = weeklyFullMenu[selectedDay] ?: emptyList()
            } finally {
                isLoading = false
            }
        }
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
