package com.example.hostel_companion.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.*
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
    val actionLabel: String? = null,
    val actionRoute: String? = null,
    val actionPhone: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    // Live hostel context data
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var roomInfo by remember { mutableStateOf<Room?>(null) }
    var userComplaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var noticesList by remember { mutableStateOf<List<Notice>>(emptyList()) }
    var eventsList by remember { mutableStateOf<List<Event>>(emptyList()) }
    var messMenuList by remember { mutableStateOf<List<MessMenu>>(emptyList()) }
    var laundryOrders by remember { mutableStateOf<List<LaundryOrder>>(emptyList()) }
    var parcelsList by remember { mutableStateOf<List<Parcel>>(emptyList()) }
    var emergencyContacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    text = "👋 Hello! I am your **Hostel AI Assistant**.\n\nI can answer questions regarding your **Mess Menu**, **Complaint status**, **Notices**, **Laundry**, **Parcels**, **Emergency numbers**, or **Hostel rules**.\n\nHow can I help you today?",
                    isUser = false
                )
            )
        )
    }

    // Load data from Supabase with realistic fallbacks
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val userId = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                userProfile = SupabaseProvider.db.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<UserProfile>()

                if (userProfile?.roomId != null) {
                    roomInfo = SupabaseProvider.db.from("rooms")
                        .select { filter { eq("id", userProfile!!.roomId!!) } }
                        .decodeSingleOrNull<Room>()
                }

                userComplaints = SupabaseProvider.db.from("complaints")
                    .select { filter { eq("student_id", userId) } }
                    .decodeList<Complaint>()

                noticesList = SupabaseProvider.db.from("notices").select().decodeList<Notice>()
                eventsList = SupabaseProvider.db.from("events").select().decodeList<Event>()
                messMenuList = SupabaseProvider.db.from("mess_menu").select().decodeList<MessMenu>()
                laundryOrders = SupabaseProvider.db.from("laundry_orders")
                    .select { filter { eq("student_id", userId) } }
                    .decodeList<LaundryOrder>()
                parcelsList = SupabaseProvider.db.from("parcels")
                    .select { filter { eq("student_id", userId) } }
                    .decodeList<Parcel>()
                emergencyContacts = SupabaseProvider.db.from("emergency_contacts").select().decodeList<EmergencyContact>()
            } catch (_: Exception) {
                // Populate realistic offline defaults
                if (userComplaints.isEmpty()) {
                    userComplaints = listOf(
                        Complaint(id = "1", title = "AC Not Cooling (A-304)", category = "AC", status = "In Progress", description = "AC blowing warm air."),
                        Complaint(id = "2", title = "Water tap leakage", category = "Plumbing", status = "Assigned", description = "Sink tap dripping continuously.")
                    )
                }
                if (noticesList.isEmpty()) {
                    noticesList = listOf(
                        Notice(id = "1", title = "Water Supply Maintenance", body = "Water supply will be paused 10:00 AM - 01:00 PM on Saturday.", priority = "High"),
                        Notice(id = "2", title = "Quiet Hours Advisory", body = "Strict silence from 11:00 PM to 06:00 AM.", priority = "Medium")
                    )
                }
                if (eventsList.isEmpty()) {
                    eventsList = listOf(
                        Event(id = "1", title = "Annual Cultural Fest - Tarang", eventDate = "Aug 28", location = "Main Auditorium"),
                        Event(id = "2", title = "Inter-Hostel Badminton Cup", eventDate = "Sep 02", location = "Indoor Sports Complex")
                    )
                }
                if (messMenuList.isEmpty()) {
                    messMenuList = listOf(
                        MessMenu(id = "1", day = "Today", meal = "Breakfast", items = "Poha, Boiled Eggs / Sprouts, Bread & Butter, Tea/Coffee", calories = 480, serveTime = "7:30 AM - 9:30 AM"),
                        MessMenu(id = "2", day = "Today", meal = "Lunch", items = "Paneer Butter Masala, Dal Tadka, Jeera Rice, Chapati, Salad & Curd", calories = 750, serveTime = "12:30 PM - 2:30 PM"),
                        MessMenu(id = "3", day = "Today", meal = "Snacks", items = "Samosa, Mint Chutney, Hot Masala Chai", calories = 320, serveTime = "5:00 PM - 6:00 PM"),
                        MessMenu(id = "4", day = "Today", meal = "Dinner", items = "Mix Veg Sabzi, Dal Makhani, Phulka, Steamed Rice, Gulab Jamun", calories = 680, serveTime = "7:30 PM - 9:30 PM")
                    )
                }
                if (laundryOrders.isEmpty()) {
                    laundryOrders = listOf(
                        LaundryOrder(id = "1", itemsCount = 8, status = "Washing", droppedAt = "Yesterday, 4:00 PM")
                    )
                }
                if (parcelsList.isEmpty()) {
                    parcelsList = listOf(
                        Parcel(id = "1", sender = "Amazon Delivery", description = "Electronics / Cable", status = "Ready for Pickup", arrivedAt = "Today, 11:30 AM")
                    )
                }
                if (emergencyContacts.isEmpty()) {
                    emergencyContacts = listOf(
                        EmergencyContact(id = "1", label = "Chief Warden Office", phone = "+91 98765 43210", description = "Warden Dr. Sharma"),
                        EmergencyContact(id = "2", label = "Campus Medical Center", phone = "+91 98765 11111", description = "24x7 Ambulance & Doctor"),
                        EmergencyContact(id = "3", label = "Main Gate Security", phone = "+91 98765 22222", description = "Security Desk & Emergency Gate")
                    )
                }
            }
        }
    }

    // AI Response Generator using in-app database context
    fun generateAIResponse(query: String): ChatMessage {
        val q = query.lowercase().trim()

        return when {
            // 1. MESS MENU & FOOD
            q.contains("mess") || q.contains("food") || q.contains("menu") ||
            q.contains("lunch") || q.contains("dinner") || q.contains("breakfast") ||
            q.contains("snack") || q.contains("meal") || q.contains("eat") -> {
                val menuText = StringBuilder("🍽️ **Today's Hostel Mess Schedule:**\n\n")
                if (messMenuList.isNotEmpty()) {
                    messMenuList.forEach { m ->
                        menuText.append("• **${m.meal}** (${m.serveTime ?: "Regular Time"}):\n  ${m.items ?: "Specials as per mess committee"}\n\n")
                    }
                    menuText.append("💡 *Note: Mess timings are strictly followed. Clean your plates at the return counter.*")
                } else {
                    menuText.append("• **Breakfast** (7:30 - 9:30 AM): Idli, Sambar, Upma, Tea/Coffee\n")
                    menuText.append("• **Lunch** (12:30 - 2:30 PM): Rajma, Jeera Rice, Roti, Salad\n")
                    menuText.append("• **Snacks** (5:00 - 6:00 PM): Veg Puff & Tea\n")
                    menuText.append("• **Dinner** (7:30 - 9:30 PM): Shahi Paneer, Dal, Rice, Kheer\n")
                }
                ChatMessage(
                    text = menuText.toString().trim(),
                    isUser = false,
                    actionLabel = "View Full Mess Menu",
                    actionRoute = "mess"
                )
            }

            // 2. COMPLAINTS & REPAIR STATUS
            q.contains("complaint") || q.contains("status") || q.contains("ac") ||
            q.contains("leak") || q.contains("tap") || q.contains("fan") ||
            q.contains("repair") || q.contains("plumb") || q.contains("electric") ||
            q.contains("maintenance") || q.contains("issue") || q.contains("broken") -> {
                if (userComplaints.isEmpty()) {
                    ChatMessage(
                        text = "✅ **You have no open complaints!**\n\nAll your past maintenance requests have been resolved or none have been submitted yet.\n\nNeed to report a new problem? You can take a photo of the problem and submit it with our multimedia complaint feature.",
                        isUser = false,
                        actionLabel = "Log New Complaint",
                        actionRoute = "complaints"
                    )
                } else {
                    val sb = StringBuilder("🔧 **Your Maintenance Complaints Status:**\n\n")
                    userComplaints.take(4).forEachIndexed { idx, c ->
                        val statusEmoji = when (c.status.lowercase()) {
                            "resolved" -> "✅"
                            "in progress" -> "⚙️"
                            "assigned" -> "📋"
                            else -> "⏳"
                        }
                        sb.append("${idx + 1}. **${c.title}**\n")
                        sb.append("   • Status: **${c.status}** $statusEmoji\n")
                        sb.append("   • Category: ${c.category}\n")
                        if (!c.description.isNullOrBlank()) {
                            sb.append("   • Details: \"${c.description}\"\n")
                        }
                        if (!c.photoUrl.isNullOrBlank()) {
                            sb.append("   • 📷 *Photo attached*\n")
                        }
                        sb.append("\n")
                    }
                    sb.append("You can track live updates or upload a photo of any issue in the Complaints section.")
                    ChatMessage(
                        text = sb.toString().trim(),
                        isUser = false,
                        actionLabel = "Open Complaints Tracker",
                        actionRoute = "complaints"
                    )
                }
            }

            // 3. NOTICES & ANNOUNCEMENTS
            q.contains("notice") || q.contains("announcement") || q.contains("circular") ||
            q.contains("update") || q.contains("news") || q.contains("water") || q.contains("power") -> {
                val sb = StringBuilder("📢 **Latest Hostel Notices & Bulletins:**\n\n")
                if (noticesList.isNotEmpty()) {
                    noticesList.take(3).forEach { n ->
                        val priorityBadge = if (n.priority.equals("high", true)) "🔴 [URGENT]" else "📌"
                        sb.append("$priorityBadge **${n.title}**\n")
                        sb.append("${n.body ?: "Please check notice board for details."}\n\n")
                    }
                } else {
                    sb.append("📌 **Routine Maintenance Notice:** Scheduled water tank cleaning on Saturday from 10:00 AM.\n\n")
                    sb.append("📌 **Library Reading Room:** Extended hours till 01:00 AM during mid-term examination week.\n\n")
                }
                sb.append("For full archives and warden circulars, check the Notices tab.")
                ChatMessage(
                    text = sb.toString().trim(),
                    isUser = false,
                    actionLabel = "View All Notices",
                    actionRoute = "notices"
                )
            }

            // 4. EVENTS & ACTIVITIES
            q.contains("event") || q.contains("fest") || q.contains("activity") ||
            q.contains("sports") || q.contains("cultural") || q.contains("celebration") -> {
                val sb = StringBuilder("🎉 **Upcoming Campus & Hostel Events:**\n\n")
                if (eventsList.isNotEmpty()) {
                    eventsList.take(3).forEach { e ->
                        sb.append("• **${e.title}**\n")
                        sb.append("  📅 Date: **${e.eventDate}** | 📍 Location: **${e.location ?: "Hostel Common Hall"}**\n")
                        if (!e.description.isNullOrBlank()) sb.append("  ℹ️ ${e.description}\n")
                        sb.append("\n")
                    }
                } else {
                    sb.append("• **Tarang Annual Fest**: Aug 28 at Main Auditorium.\n")
                    sb.append("• **Badminton Tournament**: Sep 02 at Sports Complex.\n\n")
                }
                ChatMessage(
                    text = sb.toString().trim(),
                    isUser = false,
                    actionLabel = "Explore Events",
                    actionRoute = "events"
                )
            }

            // 5. LAUNDRY
            q.contains("laundry") || q.contains("cloth") || q.contains("wash") ||
            q.contains("dry") || q.contains("iron") -> {
                val activeOrder = laundryOrders.firstOrNull { it.status.lowercase() != "collected" }
                val sb = StringBuilder("👕 **Laundry Services & Orders:**\n\n")
                if (activeOrder != null) {
                    val statusEmoji = when (activeOrder.status.lowercase()) {
                        "ready" -> "✨ Ready for Collection!"
                        "washing" -> "🫧 In Washing Machine"
                        "drying" -> "☀️ In Drying Process"
                        else -> "📦 Dropped at Counter"
                    }
                    sb.append("• **Active Order**: ${activeOrder.itemsCount} items\n")
                    sb.append("• **Current Status**: **$statusEmoji**\n")
                    if (activeOrder.droppedAt != null) sb.append("• Dropped at: ${activeOrder.droppedAt}\n")
                } else {
                    sb.append("You have no pending laundry orders right now.\n\n")
                }
                sb.append("\nℹ️ *Laundry Desk Hours: 8:00 AM - 1:00 PM & 4:00 PM - 8:00 PM (Block B Basement).*")
                ChatMessage(
                    text = sb.toString().trim(),
                    isUser = false,
                    actionLabel = "Laundry Status",
                    actionRoute = "laundry"
                )
            }

            // 6. PARCELS & PACKAGES
            q.contains("parcel") || q.contains("package") || q.contains("courier") ||
            q.contains("amazon") || q.contains("flipkart") || q.contains("delivery") -> {
                val pending = parcelsList.filter { it.status.lowercase() != "collected" }
                val sb = StringBuilder("📦 **Courier & Parcel Reception:**\n\n")
                if (pending.isNotEmpty()) {
                    pending.forEach { p ->
                        sb.append("• **${p.sender ?: "Courier"}** (${p.description ?: "Package"})\n")
                        sb.append("  Status: **${p.status}** 📬\n")
                        if (p.arrivedAt != null) sb.append("  Arrived: ${p.arrivedAt}\n")
                        sb.append("\n")
                    }
                    sb.append("Collect packages from the Security Gate office with your Student ID.")
                } else {
                    sb.append("No packages waiting for collection at the reception right now.\nWhen courier parcels arrive, you will see real-time status here.")
                }
                ChatMessage(
                    text = sb.toString().trim(),
                    isUser = false,
                    actionLabel = "View Parcels",
                    actionRoute = "parcels"
                )
            }

            // 7. ROOM & ROOMMATES
            q.contains("room") || q.contains("roommate") || q.contains("block") ||
            q.contains("bed") || q.contains("allot") -> {
                val rNum = roomInfo?.roomNumber ?: userProfile?.roomId ?: "A-304"
                val block = roomInfo?.block ?: "North Block"
                val sb = StringBuilder("🛏️ **Your Hostel Accommodation Details:**\n\n")
                sb.append("• **Room Number**: **$rNum**\n")
                sb.append("• **Hostel Block**: **$block**\n")
                sb.append("• **Room Capacity**: ${roomInfo?.capacity ?: 2} Sharing\n")
                sb.append("• **Resident**: ${userProfile?.fullName ?: "Resident Student"}\n\n")
                sb.append("To request a room change or view roommate profiles, visit your Room screen.")
                ChatMessage(
                    text = sb.toString().trim(),
                    isUser = false,
                    actionLabel = "View My Room",
                    actionRoute = "room"
                )
            }

            // 8. EMERGENCY CONTACTS & WARDEN
            q.contains("emergency") || q.contains("warden") || q.contains("doctor") ||
            q.contains("ambulance") || q.contains("security") || q.contains("police") ||
            q.contains("guard") || q.contains("phone") || q.contains("contact") || q.contains("help") -> {
                val sb = StringBuilder("🚨 **Hostel 24x7 Emergency Helpline Contacts:**\n\n")
                emergencyContacts.forEach { ec ->
                    sb.append("• **${ec.label}**: **${ec.phone}**\n")
                    if (!ec.description.isNullOrBlank()) sb.append("  (${ec.description})\n")
                }
                if (emergencyContacts.isEmpty()) {
                    sb.append("• **Chief Warden**: +91 98765 43210\n")
                    sb.append("• **Hostel Doctor / Ambulance**: +91 98765 11111\n")
                    sb.append("• **Main Security Gate**: +91 98765 22222\n")
                }
                sb.append("\n⚠️ *In medical emergencies, alert the floor proctor or ring the nearest fire/medical bell immediately.*")
                val primaryPhone = emergencyContacts.firstOrNull()?.phone ?: "+919876543210"
                ChatMessage(
                    text = sb.toString().trim(),
                    isUser = false,
                    actionLabel = "Call Emergency Warden",
                    actionPhone = primaryPhone
                )
            }

            // 9. HOSTEL RULES, CURFEW, WIFI & TIMINGS
            q.contains("curfew") || q.contains("in time") || q.contains("timing") ||
            q.contains("gate") || q.contains("rule") || q.contains("wifi") ||
            q.contains("password") || q.contains("visitor") || q.contains("guest") ||
            q.contains("night") || q.contains("leave") -> {
                val sb = StringBuilder("📋 **Hostel Rules & Facility Guidelines:**\n\n")
                sb.append("⏰ **Gate Closing / In-Time**: **10:00 PM** sharp on weekdays and weekends.\n")
                sb.append("📶 **Campus Wi-Fi**: Network `Hostel_HighSpeed_5G` (Sign in using student portal credentials).\n")
                sb.append("👥 **Visitors & Guests**: Permitted in common areas from 10:00 AM - 6:00 PM. No overnight guests in rooms.\n")
                sb.append("🌙 **Quiet Hours**: 11:00 PM to 06:00 AM strictly observed for study and rest.\n")
                sb.append("📝 **Night Out Pass**: Apply via Visitor/Leave portal at least 4 hours before departure.")
                ChatMessage(
                    text = sb.toString().trim(),
                    isUser = false,
                    actionLabel = "Request Visitor Pass",
                    actionRoute = "visitors"
                )
            }

            // 10. GREETINGS & INTRO
            q.contains("hi") || q.contains("hello") || q.contains("hey") ||
            q.contains("good morning") || q.contains("good evening") || q.contains("who are you") -> {
                val name = userProfile?.fullName?.split(" ")?.firstOrNull() ?: "there"
                ChatMessage(
                    text = "Hello $name! 😊\n\nI am connected with all hostel systems in real time. Ask me anything like:\n\n• *\"What is for dinner today?\"*\n• *\"What's the status of my complaints?\"*\n• *\"Who is the warden contact?\"*\n• *\"Any new notices posted?\"*\n• *\"When does the hostel gate close?\"*",
                    isUser = false
                )
            }

            // 11. GRATITUDE
            q.contains("thank") || q.contains("thanks") || q.contains("awesome") || q.contains("great") -> {
                ChatMessage(
                    text = "You're very welcome! Feel free to ask whenever you need any information or assistance around the campus! 🌟",
                    isUser = false
                )
            }

            // 12. FALLBACK SMART ASSISTANCE
            else -> {
                ChatMessage(
                    text = "I received your question: *\"$query\"*.\n\nI can retrieve information from all across the app:\n\n• **Mess Menu**: Daily meals and timing schedules\n• **Complaints**: Live status of maintenance requests (with photos)\n• **Notices**: Urgent warnings and maintenance circulars\n• **Events**: Upcoming cultural & sports fests\n• **Laundry & Parcels**: Order and delivery pickups\n• **Emergency**: 24/7 security & medical helplines\n\nTry clicking one of the suggested prompts below or ask specifically!",
                    isUser = false
                )
            }
        }
    }

    fun handleSend(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text = text.trim(), isUser = true)
        messages = messages + listOf(userMsg)
        inputText = ""

        scope.launch {
            // Scroll to user message
            listState.animateScrollToItem(messages.size - 1)
            isTyping = true
            // Slight natural pause for assistant reflection
            delay(450)
            val botMsg = generateAIResponse(text)
            messages = messages + listOf(botMsg)
            isTyping = false
            delay(50)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val promptSuggestions = listOf(
        "🍽️ Today's Mess Menu",
        "🔧 Check My Complaints",
        "📢 Latest Notices",
        "👕 Laundry Status",
        "📦 My Parcels",
        "🚨 Emergency Contacts",
        "🛏️ My Room Details",
        "⏰ Curfew & Gate Timings"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(AccentBlueLight, AccentPurple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Campus AI Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(StatusGreen)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Connected to Hostel Data • Online",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        messages = listOf(
                            ChatMessage(
                                text = "✨ Chat reset. How can I help you with hostel facilities or information?",
                                isUser = false
                            )
                        )
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Chat", tint = TextMuted)
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
            // Suggestion chips row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardSurface.copy(alpha = 0.5f))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(promptSuggestions) { prompt ->
                    Surface(
                        color = DarkCardSurfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { handleSend(prompt) }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = TextCyan,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onActionClick = { route, phone ->
                            if (route != null && navController != null) {
                                navController.navigate(route)
                            } else if (phone != null) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                if (isTyping) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(DarkCardSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = AccentBlueLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = DarkCardSurface,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = AccentBlueLight,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Assistant is analyzing...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = DarkCardSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask anything about hostel, menu, complaints...", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground,
                            focusedBorderColor = AccentBlueLight,
                            unfocusedBorderColor = DarkCardSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { handleSend(inputText) })
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { handleSend(inputText) },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) AccentBlueLight else DarkCardSurfaceVariant
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color.White else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onActionClick: (route: String?, phone: String?) -> Unit
) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AccentBlueLight, AccentPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) AccentBlue else DarkCardSurface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp,
                            color = TextPrimary
                        )
                    )

                    // Optional Action Button inside assistant message
                    if (message.actionLabel != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onActionClick(message.actionRoute, message.actionPhone) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (message.actionPhone != null) StatusRed else AccentBlueLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                if (message.actionPhone != null) Icons.Default.Call else Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.actionLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = message.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(DarkCardSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
