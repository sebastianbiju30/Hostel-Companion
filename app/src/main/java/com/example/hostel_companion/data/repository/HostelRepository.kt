package com.example.hostel_companion.data.repository

import com.example.hostel_companion.data.models.*
import com.example.hostel_companion.data.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Single source of truth repository for all hostel data.
 * Ensures that changes made in the Warden section immediately reflect across
 * all student screens (and vice-versa), while syncing with Supabase in the background.
 */
object HostelRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Standard valid UUID constants for seamless database interop
    const val ID_COMPLAINT_1 = "00000000-0000-0000-0000-000000000001"
    const val ID_COMPLAINT_2 = "00000000-0000-0000-0000-000000000002"
    const val ID_COMPLAINT_3 = "00000000-0000-0000-0000-000000000003"

    const val ID_VISITOR_1 = "00000000-0000-0000-0000-000000000011"
    const val ID_VISITOR_2 = "00000000-0000-0000-0000-000000000012"

    const val ID_LAUNDRY_1 = "00000000-0000-0000-0000-000000000021"
    const val ID_LAUNDRY_2 = "00000000-0000-0000-0000-000000000022"

    const val ID_PARCEL_1 = "00000000-0000-0000-0000-000000000031"
    const val ID_PARCEL_2 = "00000000-0000-0000-0000-000000000032"

    const val ID_EVENT_1 = "00000000-0000-0000-0000-000000000041"
    const val ID_EVENT_2 = "00000000-0000-0000-0000-000000000042"
    const val ID_EVENT_3 = "00000000-0000-0000-0000-000000000043"

    const val ID_ROOM_1 = "00000000-0000-0000-0000-000000000051"
    const val ID_ROOM_2 = "00000000-0000-0000-0000-000000000052"

    // 1. Complaints
    private val _complaints = MutableStateFlow<List<Complaint>>(
        listOf(
            Complaint(
                id = ID_COMPLAINT_1,
                title = "AC Not Cooling (Room A-304)",
                description = "Warm air coming from AC unit in Room A-304 continuously. Compressor not engaging.",
                category = "AC",
                status = "In Progress",
                createdAt = "2026-10-21T10:30:00Z"
            ),
            Complaint(
                id = ID_COMPLAINT_2,
                title = "Washroom Water Pipe Leakage",
                description = "Sink drainage pipe dripping continuously in 3rd floor washroom.",
                category = "Plumbing",
                status = "Submitted",
                createdAt = "2026-10-22T08:15:00Z"
            ),
            Complaint(
                id = ID_COMPLAINT_3,
                title = "Ceiling Fan Clicking Noise",
                description = "Fan regulator sparks on speed 4 and produces continuous humming noise.",
                category = "Electrical",
                status = "Assigned",
                createdAt = "2026-10-20T14:00:00Z"
            )
        )
    )
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    // 2. Mess Menu (Full 7-day schedule)
    private val _messMenu = MutableStateFlow<List<MessMenu>>(
        listOf(
            // Monday
            MessMenu(id = "m1", day = "Monday", meal = "Breakfast", items = "Aloo Paratha with Curd, Boiled Eggs / Banana, Butter & Jam, Masala Tea", calories = 520, serveTime = "07:30 AM - 09:30 AM"),
            MessMenu(id = "m2", day = "Monday", meal = "Lunch", items = "Rajma Masala, Steamed Jeera Rice, Phulka, Boondi Raita, Fresh Green Salad", calories = 740, serveTime = "12:30 PM - 02:30 PM"),
            MessMenu(id = "m3", day = "Monday", meal = "Snacks", items = "Crispy Veg Cutlet with Mint Dip, Filter Coffee / Tea", calories = 310, serveTime = "05:00 PM - 06:00 PM"),
            MessMenu(id = "m4", day = "Monday", meal = "Dinner", items = "Kadai Paneer, Yellow Dal Fry, Tawa Roti, Rice, Fruit Custard", calories = 680, serveTime = "07:30 PM - 09:30 PM"),

            // Tuesday
            MessMenu(id = "t1", day = "Tuesday", meal = "Breakfast", items = "Masala Dosa with Coconut & Tomato Chutney, Sambar, Filter Coffee", calories = 480, serveTime = "07:30 AM - 09:30 AM"),
            MessMenu(id = "t2", day = "Tuesday", meal = "Lunch", items = "Chole Bhature / Kulche, Pulao, Curd, Pickled Onions", calories = 820, serveTime = "12:30 PM - 02:30 PM"),
            MessMenu(id = "t3", day = "Tuesday", meal = "Snacks", items = "Bhel Puri / Sev Puri, Hot Masala Chai", calories = 260, serveTime = "05:00 PM - 06:00 PM"),
            MessMenu(id = "t4", day = "Tuesday", meal = "Dinner", items = "Palak Paneer, Moong Dal Khichdi, Phulka, Papad, Gulab Jamun", calories = 660, serveTime = "07:30 PM - 09:30 PM"),

            // Wednesday
            MessMenu(id = "w1", day = "Wednesday", meal = "Breakfast", items = "Idli & Vada with Sambar, Chutney, Bread Omelette / Sprouts, Tea", calories = 490, serveTime = "07:30 AM - 09:30 AM"),
            MessMenu(id = "w2", day = "Wednesday", meal = "Lunch", items = "Paneer Butter Masala, Dal Tadka, Ghee Rice, Roti, Cucumber Salad", calories = 760, serveTime = "12:30 PM - 02:30 PM"),
            MessMenu(id = "w3", day = "Wednesday", meal = "Snacks", items = "Hot Onion Pakoda, Green Chutney, Ginger Chai", calories = 340, serveTime = "05:00 PM - 06:00 PM"),
            MessMenu(id = "w4", day = "Wednesday", meal = "Dinner", items = "Egg Curry / Malai Kofta, Dal Makhani, Phulka, Jeera Rice, Ice Cream", calories = 710, serveTime = "07:30 PM - 09:30 PM"),

            // Thursday
            MessMenu(id = "th1", day = "Thursday", meal = "Breakfast", items = "Poha with Sev & Peanuts, Boiled Eggs / Fruit Bowl, Tea / Coffee", calories = 460, serveTime = "07:30 AM - 09:30 AM"),
            MessMenu(id = "th2", day = "Thursday", meal = "Lunch", items = "Kadhi Pakoda, Aloo Jeera, Steamed Basmati Rice, Chapati, Salad", calories = 690, serveTime = "12:30 PM - 02:30 PM"),
            MessMenu(id = "th3", day = "Thursday", meal = "Snacks", items = "Samosa with Tamarind Chutney, Masala Chai", calories = 320, serveTime = "05:00 PM - 06:00 PM"),
            MessMenu(id = "th4", day = "Thursday", meal = "Dinner", items = "Shahi Veg Biryani, Mirchi Ka Salan, Veg Raita, Sweet Kheer", calories = 750, serveTime = "07:30 PM - 09:30 PM"),

            // Friday
            MessMenu(id = "f1", day = "Friday", meal = "Breakfast", items = "Uttapam with Sambar, Coconut Chutney, Scrambled Eggs / Sprouted Moong", calories = 470, serveTime = "07:30 AM - 09:30 AM"),
            MessMenu(id = "f2", day = "Friday", meal = "Lunch", items = "Dal Palak, Mix Veg Dry, Steamed Rice, Tawa Roti, Curd", calories = 670, serveTime = "12:30 PM - 02:30 PM"),
            MessMenu(id = "f3", day = "Friday", meal = "Snacks", items = "Grilled Veg Sandwich, Hot Bournvita / Tea", calories = 290, serveTime = "05:00 PM - 06:00 PM"),
            MessMenu(id = "f4", day = "Friday", meal = "Dinner", items = "Matar Paneer, Dal Fry, Phulka, Rice, Rasgulla", calories = 690, serveTime = "07:30 PM - 09:30 PM"),

            // Saturday
            MessMenu(id = "s1", day = "Saturday", meal = "Breakfast", items = "Poori Bhaji (Aloo Tamatar), Boiled Eggs, Halwa, Masala Chai", calories = 580, serveTime = "08:00 AM - 10:00 AM"),
            MessMenu(id = "s2", day = "Saturday", meal = "Lunch", items = "Lemon Rice, Sambar, Potato Roast, Curd Rice, Fryums", calories = 720, serveTime = "12:30 PM - 02:30 PM"),
            MessMenu(id = "s3", day = "Saturday", meal = "Snacks", items = "Pav Bhaji (2 Butter Pav), Lemonade / Tea", calories = 380, serveTime = "05:00 PM - 06:00 PM"),
            MessMenu(id = "s4", day = "Saturday", meal = "Dinner", items = "Veg Manchurian with Fried Rice / Noodles, Sweet Corn Soup", calories = 730, serveTime = "07:30 PM - 09:30 PM"),

            // Sunday
            MessMenu(id = "su1", day = "Sunday", meal = "Breakfast", items = "Executive Weekend Brunch: Chole Bhature, Dosa, Eggs to order, Fresh Juice", calories = 650, serveTime = "08:00 AM - 10:30 AM"),
            MessMenu(id = "su2", day = "Sunday", meal = "Lunch", items = "Sunday Feast: Chicken Dum Biryani / Shahi Paneer Biryani, Mirchi Salan, Raita, Gulab Jamun", calories = 890, serveTime = "12:30 PM - 02:45 PM"),
            MessMenu(id = "su3", day = "Sunday", meal = "Snacks", items = "French Fries with Cheese Dip, Cold Coffee / Hot Tea", calories = 350, serveTime = "05:00 PM - 06:00 PM"),
            MessMenu(id = "su4", day = "Sunday", meal = "Dinner", items = "Light Dinner: Dal Tadka, Seasonal Sabzi, Steamed Rice, Soft Phulkas, Fresh Cut Fruits", calories = 590, serveTime = "07:30 PM - 09:30 PM")
        )
    )
    val messMenu: StateFlow<List<MessMenu>> = _messMenu.asStateFlow()

    // 3. Visitor Passes
    private val _visitors = MutableStateFlow<List<Visitor>>(
        listOf(
            Visitor(
                id = ID_VISITOR_1,
                visitorName = "Suresh Biju (Parent)",
                visitorPhone = "+91 98765 43210",
                purpose = "Weekend Family Visit & Fee Verification",
                status = "Pending",
                requestedAt = "Today 10:30 AM"
            ),
            Visitor(
                id = ID_VISITOR_2,
                visitorName = "Anil Kumar (Project Partner)",
                visitorPhone = "+91 98765 22222",
                purpose = "Hackathon Preparation & Lab Work",
                status = "Approved",
                requestedAt = "Yesterday 04:15 PM"
            )
        )
    )
    val visitors: StateFlow<List<Visitor>> = _visitors.asStateFlow()

    // 4. Events
    private val _events = MutableStateFlow<List<Event>>(
        listOf(
            Event(
                id = ID_EVENT_1,
                title = "Tarang - Annual Cultural & Music Fest",
                description = "Live DJ, inter-hostel dance & acoustic battles, food trucks, and celebrity concert night! Free entry for all residents.",
                location = "Campus Open Air Amphitheatre",
                eventDate = "Sat, Nov 08 • 06:00 PM"
            ),
            Event(
                id = ID_EVENT_2,
                title = "Hostel Premier League (HPL) T20 Cricket",
                description = "North Block vs South Block clash in the opening match of the annual inter-block trophy. Come cheer for your block!",
                location = "Hostel Main Sports Ground",
                eventDate = "Sun, Nov 09 • 04:00 PM"
            ),
            Event(
                id = ID_EVENT_3,
                title = "CodeNight 12-Hour Campus Hackathon",
                description = "Build exciting Android & Web AI applications with free pizza, energy drinks, and cash prizes for top 3 teams.",
                location = "Hostel Common Tech Room",
                eventDate = "Wed, Nov 12 • 07:00 PM"
            )
        )
    )
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    // 5. Laundry Orders
    private val _laundryOrders = MutableStateFlow<List<LaundryOrder>>(
        listOf(
            LaundryOrder(id = ID_LAUNDRY_1, itemsCount = 7, status = "Washing", droppedAt = "Yesterday, 04:30 PM"),
            LaundryOrder(id = ID_LAUNDRY_2, itemsCount = 4, status = "Ready", droppedAt = "Oct 20, 11:00 AM")
        )
    )
    val laundryOrders: StateFlow<List<LaundryOrder>> = _laundryOrders.asStateFlow()

    // 6. Parcels
    private val _parcels = MutableStateFlow<List<Parcel>>(
        listOf(
            Parcel(id = ID_PARCEL_1, sender = "Amazon Delivery", description = "Laptop Charger & Electronics", status = "Ready for Pickup", arrivedAt = "Today, 11:30 AM"),
            Parcel(id = ID_PARCEL_2, sender = "Flipkart Package", description = "Engineering Books & Study Notes", status = "Arrived", arrivedAt = "Yesterday, 04:00 PM")
        )
    )
    val parcels: StateFlow<List<Parcel>> = _parcels.asStateFlow()

    // 7. Student Profiles & Rooms
    private val _rooms = MutableStateFlow<List<Room>>(
        listOf(
            Room(id = ID_ROOM_1, roomNumber = "A-304", block = "North Block", capacity = 2),
            Room(id = ID_ROOM_2, roomNumber = "B-102", block = "South Block", capacity = 2)
        )
    )
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _profiles = MutableStateFlow<List<UserProfile>>(
        listOf(
            UserProfile(id = "p1", fullName = "Sebastian Biju", email = "sebastianbiju30@gmail.com", role = "student", roomId = ID_ROOM_1),
            UserProfile(id = "p2", fullName = "Rahul Sharma", email = "rahul.sharma@hostel.edu", role = "student", roomId = ID_ROOM_1),
            UserProfile(id = "p3", fullName = "Amit Patel", email = "amit.patel@hostel.edu", role = "student", roomId = ID_ROOM_2),
            UserProfile(id = "p4", fullName = "Karan Verma", email = "karan.v@hostel.edu", role = "student", roomId = null)
        )
    )
    val profiles: StateFlow<List<UserProfile>> = _profiles.asStateFlow()

    init {
        refreshFromSupabase()
    }

    /**
     * Pulls latest data from Supabase if available without wiping local state if offline.
     */
    fun refreshFromSupabase() {
        scope.launch {
            try {
                val dbComplaints = SupabaseProvider.db.from("complaints").select().decodeList<Complaint>()
                if (dbComplaints.isNotEmpty()) _complaints.value = dbComplaints
            } catch (_: Exception) {}

            try {
                val dbMenu = SupabaseProvider.db.from("mess_menu").select().decodeList<MessMenu>()
                if (dbMenu.isNotEmpty()) _messMenu.value = dbMenu
            } catch (_: Exception) {}

            try {
                val dbVisitors = SupabaseProvider.db.from("visitors").select().decodeList<Visitor>()
                if (dbVisitors.isNotEmpty()) _visitors.value = dbVisitors
            } catch (_: Exception) {}

            try {
                val dbEvents = SupabaseProvider.db.from("events").select().decodeList<Event>()
                if (dbEvents.isNotEmpty()) _events.value = dbEvents
            } catch (_: Exception) {}

            try {
                val dbLaundry = SupabaseProvider.db.from("laundry_orders").select().decodeList<LaundryOrder>()
                if (dbLaundry.isNotEmpty()) _laundryOrders.value = dbLaundry
            } catch (_: Exception) {}

            try {
                val dbParcels = SupabaseProvider.db.from("parcels").select().decodeList<Parcel>()
                if (dbParcels.isNotEmpty()) _parcels.value = dbParcels
            } catch (_: Exception) {}

            try {
                val dbRooms = SupabaseProvider.db.from("rooms").select().decodeList<Room>()
                if (dbRooms.isNotEmpty()) _rooms.value = dbRooms
            } catch (_: Exception) {}

            try {
                val dbProfiles = SupabaseProvider.db.from("profiles").select().decodeList<UserProfile>()
                if (dbProfiles.isNotEmpty()) _profiles.value = dbProfiles
            } catch (_: Exception) {}
        }
    }

    // ------------------------------------------------------------------------
    // MUTATION METHODS (Immediate local update + Supabase background sync)
    // ------------------------------------------------------------------------

    fun updateComplaintStatus(complaintId: String, newStatus: String) {
        _complaints.value = _complaints.value.map {
            if (it.id == complaintId) it.copy(status = newStatus) else it
        }
        scope.launch {
            try {
                SupabaseProvider.db.from("complaints")
                    .update({ set("status", newStatus) }) {
                        filter { eq("id", complaintId) }
                    }
            } catch (_: Exception) {}
        }
    }

    fun addComplaint(complaint: Complaint) {
        _complaints.value = listOf(complaint) + _complaints.value
        scope.launch {
            try {
                SupabaseProvider.db.from("complaints").insert(complaint)
            } catch (_: Exception) {}
        }
    }

    fun updateMessMenuItem(mealId: String, items: String, serveTime: String, calories: Int) {
        _messMenu.value = _messMenu.value.map {
            if (it.id == mealId) it.copy(items = items, serveTime = serveTime, calories = calories) else it
        }
        scope.launch {
            try {
                SupabaseProvider.db.from("mess_menu")
                    .update({
                        set("items", items)
                        set("serve_time", serveTime)
                        set("calories", calories)
                    }) {
                        filter { eq("id", mealId) }
                    }
            } catch (_: Exception) {}
        }
    }

    fun updateVisitorStatus(visitorId: String, newStatus: String) {
        _visitors.value = _visitors.value.map {
            if (it.id == visitorId) it.copy(status = newStatus) else it
        }
        scope.launch {
            try {
                SupabaseProvider.db.from("visitors")
                    .update({ set("status", newStatus) }) {
                        filter { eq("id", visitorId) }
                    }
            } catch (_: Exception) {}
        }
    }

    fun addVisitor(visitor: Visitor) {
        _visitors.value = listOf(visitor) + _visitors.value
        scope.launch {
            try {
                SupabaseProvider.db.from("visitors").insert(visitor)
            } catch (_: Exception) {}
        }
    }

    fun addEvent(event: Event) {
        _events.value = listOf(event) + _events.value
        scope.launch {
            try {
                SupabaseProvider.db.from("events").insert(event)
            } catch (_: Exception) {}
        }
    }

    fun deleteEvent(eventId: String) {
        _events.value = _events.value.filter { it.id != eventId }
        scope.launch {
            try {
                SupabaseProvider.db.from("events").delete { filter { eq("id", eventId) } }
            } catch (_: Exception) {}
        }
    }

    fun updateLaundryStatus(orderId: String, newStatus: String) {
        _laundryOrders.value = _laundryOrders.value.map {
            if (it.id == orderId) it.copy(status = newStatus) else it
        }
        scope.launch {
            try {
                SupabaseProvider.db.from("laundry_orders")
                    .update({ set("status", newStatus) }) {
                        filter { eq("id", orderId) }
                    }
            } catch (_: Exception) {}
        }
    }

    fun addLaundryOrder(order: LaundryOrder) {
        _laundryOrders.value = listOf(order) + _laundryOrders.value
        scope.launch {
            try {
                SupabaseProvider.db.from("laundry_orders").insert(order)
            } catch (_: Exception) {}
        }
    }

    fun updateParcelStatus(parcelId: String, newStatus: String) {
        _parcels.value = _parcels.value.map {
            if (it.id == parcelId) it.copy(status = newStatus) else it
        }
        scope.launch {
            try {
                SupabaseProvider.db.from("parcels")
                    .update({ set("status", newStatus) }) {
                        filter { eq("id", parcelId) }
                    }
            } catch (_: Exception) {}
        }
    }

    fun addParcel(parcel: Parcel) {
        _parcels.value = listOf(parcel) + _parcels.value
        scope.launch {
            try {
                SupabaseProvider.db.from("parcels").insert(parcel)
            } catch (_: Exception) {}
        }
    }

    fun allocateRoom(studentId: String, roomNumber: String, block: String) {
        // Find existing room or create new one
        val existingRoom = _rooms.value.firstOrNull { it.roomNumber.equals(roomNumber, true) }
        val targetRoomId = existingRoom?.id ?: UUID.randomUUID().toString()

        if (existingRoom == null) {
            val newRoom = Room(id = targetRoomId, roomNumber = roomNumber, block = block, capacity = 2)
            _rooms.value = _rooms.value + newRoom
            scope.launch {
                try {
                    SupabaseProvider.db.from("rooms").insert(newRoom)
                } catch (_: Exception) {}
            }
        }

        // Update student profile
        _profiles.value = _profiles.value.map {
            if (it.id == studentId) it.copy(roomId = targetRoomId) else it
        }

        scope.launch {
            try {
                SupabaseProvider.db.from("profiles")
                    .update({ set("room_id", targetRoomId) }) {
                        filter { eq("id", studentId) }
                    }
            } catch (_: Exception) {}
        }
    }
}
