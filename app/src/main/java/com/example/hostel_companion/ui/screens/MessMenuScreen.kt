package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.MessMenu
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessMenuScreen() {
    var menuItems by remember { mutableStateOf<List<MessMenu>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                menuItems = SupabaseProvider.db.from("mess_menu").select().decodeList<MessMenu>()
            } catch (_: Exception) {
                menuItems = listOf(
                    MessMenu(id = "1", day = "Today", meal = "Breakfast", items = "Poha, Boiled Eggs, Tea", calories = 450, serveTime = "07:30 AM"),
                    MessMenu(id = "2", day = "Today", meal = "Lunch", items = "Rice, Dal, Paneer Masala, Salad", calories = 850, serveTime = "12:30 PM"),
                    MessMenu(id = "3", day = "Today", meal = "Snacks", items = "Samosa, Hot Coffee", calories = 320, serveTime = "05:00 PM"),
                    MessMenu(id = "4", day = "Today", meal = "Dinner", items = "Chapati, Veg Curry, Kheer", calories = 650, serveTime = "08:00 PM")
                )
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Mess Menu & Schedule", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface)
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(menuItems) { meal ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(meal.meal, style = MaterialTheme.typography.titleMedium, color = AccentAmber)
                                    Text(meal.serveTime ?: "", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(meal.items ?: "Menu to be updated", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                meal.calories?.let { cal ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("🔥 $cal calories", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
