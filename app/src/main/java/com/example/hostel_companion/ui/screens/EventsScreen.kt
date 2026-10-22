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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.Event
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen() {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("All") }
    var registeredEventIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    val categories = listOf("All", "Cultural", "Sports", "Technical", "Social")

    // Vibrant default events list for full content
    val defaultEvents = remember {
        listOf(
            Event(
                id = "e1",
                title = "Tarang - Annual Cultural & Music Fest",
                description = "Live DJ, inter-hostel dance & acoustic battles, food trucks, and celebrity concert night! Free entry for all hostel residents.",
                location = "Campus Open Air Amphitheatre",
                eventDate = "Sat, Nov 08 • 06:00 PM"
            ),
            Event(
                id = "e2",
                title = "Hostel Premier League (HPL) T20 Cricket",
                description = "North Block vs South Block clash in the opening match of the annual inter-block trophy. Come cheer for your block!",
                location = "Hostel Main Sports Ground",
                eventDate = "Sun, Nov 09 • 04:00 PM"
            ),
            Event(
                id = "e3",
                title = "24-Hour CodeNight Hackathon 2026",
                description = "Build innovative AI & IoT solutions overnight. Free midnight pizza, energy drinks, and cash prizes worth ₹50,000.",
                location = "Innovation & Computing Lab (Block C)",
                eventDate = "Fri, Nov 14 • 07:00 PM"
            ),
            Event(
                id = "e4",
                title = "Hostel Movie Night & Open Popcorn",
                description = "Big screen projector screening of latest blockbuster selected by student poll. Bring your beanbags and blankets!",
                location = "Common Room Hall (1st Floor)",
                eventDate = "Sat, Nov 15 • 08:30 PM"
            ),
            Event(
                id = "e5",
                title = "Inter-Hostel Badminton & Table Tennis Cup",
                description = "Singles and doubles knockout tournament under lights. Medals & certificates for all finalists.",
                location = "Indoor Sports Complex",
                eventDate = "Wed, Nov 19 • 05:30 PM"
            ),
            Event(
                id = "e6",
                title = "Campus Career & Internship Orientation",
                description = "Alumni interactive panel on cracking top tech, finance, and core engineering summer internships.",
                location = "Seminar Hall 2",
                eventDate = "Mon, Nov 24 • 04:30 PM"
            )
        )
    }

    fun fetchEvents() {
        scope.launch {
            isLoading = true
            try {
                val dbEvents = SupabaseProvider.db.from("events").select().decodeList<Event>()
                events = if (dbEvents.isNotEmpty()) dbEvents else defaultEvents
            } catch (_: Exception) {
                events = defaultEvents
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchEvents()
    }

    val filteredEvents = events.filter { event ->
        when (selectedCategory) {
            "Cultural" -> event.title.contains("Cultural", true) || event.title.contains("Music", true) || event.title.contains("Movie", true)
            "Sports" -> event.title.contains("Cricket", true) || event.title.contains("Badminton", true) || event.title.contains("Sports", true)
            "Technical" -> event.title.contains("Hackathon", true) || event.title.contains("Code", true) || event.title.contains("Career", true)
            "Social" -> event.title.contains("Night", true) || event.title.contains("Fest", true)
            else -> true
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hostel & Campus Events", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text("Upcoming tournaments, cultural fests & hackathons", style = MaterialTheme.typography.bodySmall, color = AccentTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface),
                actions = {
                    IconButton(onClick = { fetchEvents() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
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
            // Category Filter Pills
            Surface(
                color = DarkCardSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentPurple,
                                selectedLabelColor = Color.White,
                                containerColor = DarkCardSurfaceVariant,
                                labelColor = TextMuted
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentPurple)
                } else if (filteredEvents.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No events found in this category.", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredEvents, key = { it.id.ifBlank { it.hashCode().toString() } }) { event ->
                            val isRegistered = registeredEventIds.contains(event.id)
                            EventCard(
                                event = event,
                                isRegistered = isRegistered,
                                onRegisterToggle = {
                                    registeredEventIds = if (isRegistered) {
                                        registeredEventIds - event.id
                                    } else {
                                        registeredEventIds + event.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    isRegistered: Boolean,
    onRegisterToggle: () -> Unit
) {
    val gradientColors = when {
        event.title.contains("Cultural", true) || event.title.contains("Music", true) -> listOf(Color(0xFF7C3AED), Color(0xFFDB2777))
        event.title.contains("Cricket", true) || event.title.contains("Sports", true) -> listOf(Color(0xFF059669), Color(0xFF0D9488))
        event.title.contains("Code", true) || event.title.contains("Hackathon", true) -> listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
        else -> listOf(Color(0xFFD97706), Color(0xFFEA580C))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Brush.horizontalGradient(gradientColors))
                    .padding(14.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.eventDate,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                if (isRegistered) {
                    Surface(
                        color = StatusGreen,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RSVP'D", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                }
            }

            // Body
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location ?: "Hostel Grounds",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextCyan
                    )
                }

                event.description?.let { desc ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRegistered) "You + 142 attending" else "142 attending",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Button(
                        onClick = onRegisterToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRegistered) DarkCardSurfaceVariant else AccentPurple
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isRegistered) "Cancel RSVP" else "Register Free",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isRegistered) TextMuted else Color.White
                        )
                    }
                }
            }
        }
    }
}
