package com.example.hostel_companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.Parcel
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelsScreen() {
    var parcels by remember { mutableStateOf<List<Parcel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                parcels = SupabaseProvider.db.from("parcels").select().decodeList<Parcel>()
            } catch (_: Exception) {
                parcels = listOf(
                    Parcel(id = "1", sender = "Amazon", description = "Books Package", status = "Ready for Pickup"),
                    Parcel(id = "2", sender = "Flipkart", description = "Electronics", status = "Arrived")
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
                title = { Text("My Parcels", style = MaterialTheme.typography.titleLarge) },
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(parcels) { parcel ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(parcel.sender ?: "Courier Parcel", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                    Text(parcel.description ?: "Package at Main Gate", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                                Surface(
                                    color = Color(0xFF1E1B4B),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        parcel.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentPurple,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
