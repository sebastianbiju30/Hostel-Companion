package com.example.hostel_companion.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.Complaint
import com.example.hostel_companion.data.models.UserProfile
import com.example.hostel_companion.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen() {
    val context = LocalContext.current
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var isUserAdmin by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val categories = listOf("All", "Electrical", "Plumbing", "AC", "Furniture", "Cleanliness", "Internet", "Other")

    fun fetchComplaints() {
        scope.launch {
            isLoading = true
            try {
                val userId = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                val profile = SupabaseProvider.db.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<UserProfile>()
                isUserAdmin = profile?.role?.lowercase() in listOf("admin", "warden") || profile?.isAdmin == true

                complaints = SupabaseProvider.db.from("complaints")
                    .select()
                    .decodeList<Complaint>()
            } catch (_: Exception) {
                // Fallback realistic complaints
                complaints = listOf(
                    Complaint(
                        id = "1",
                        title = "AC Not Cooling (Room A-304)",
                        description = "AC blowing warm air continuously. Fan speed seems low.",
                        category = "AC",
                        status = "In Progress",
                        createdAt = "2026-10-20T10:30:00Z"
                    ),
                    Complaint(
                        id = "2",
                        title = "Water tap leakage in washroom",
                        description = "Bathroom sink tap dripping and making splashing noise all night.",
                        category = "Plumbing",
                        status = "Assigned",
                        createdAt = "2026-10-19T14:15:00Z"
                    ),
                    Complaint(
                        id = "3",
                        title = "Ceiling fan making clicking sound",
                        description = "Rotor clicking loudly on speed 3 and 4.",
                        category = "Electrical",
                        status = "Resolved",
                        createdAt = "2026-10-18T09:00:00Z"
                    )
                )
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchComplaints()
    }

    val filteredComplaints = complaints.filter { complaint ->
        val matchesQuery = searchQuery.isBlank() ||
                complaint.title.contains(searchQuery, ignoreCase = true) ||
                (complaint.description?.contains(searchQuery, ignoreCase = true) == true) ||
                complaint.category.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategoryFilter == "All" ||
                complaint.category.equals(selectedCategoryFilter, ignoreCase = true)

        matchesQuery && matchesCategory
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Complaints & Maintenance",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = if (isUserAdmin) "Warden Resolution Mode" else "Track and lodge hostel issues",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isUserAdmin) AccentTeal else TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCardSurface
                ),
                actions = {
                    IconButton(onClick = { fetchComplaints() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                containerColor = AccentBlueLight,
                contentColor = TextPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Complaint", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by title, description, or category...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardSurface,
                        unfocusedContainerColor = DarkCardSurface,
                        focusedBorderColor = AccentBlueLight,
                        unfocusedBorderColor = DarkCardSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlueLight,
                                selectedLabelColor = TextPrimary,
                                containerColor = DarkCardSurface,
                                labelColor = TextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AccentBlueLight else DarkCardSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentBlueLight)
                    }
                } else if (filteredComplaints.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(52.dp)
                            )
                            Text(
                                text = "No complaints found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Everything is running smoothly or matches your filter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredComplaints, key = { it.id.ifBlank { it.hashCode().toString() } }) { complaint ->
                            ComplaintCard(
                                complaint = complaint,
                                isAdmin = isUserAdmin,
                                onImageClick = { previewImageUrl = it },
                                onStatusUpdate = { newStatus ->
                                    scope.launch {
                                        try {
                                            SupabaseProvider.db.from("complaints")
                                                .update({ set("status", newStatus) }) {
                                                    filter { eq("id", complaint.id) }
                                                }
                                            fetchComplaints()
                                        } catch (_: Exception) {
                                            complaints = complaints.map {
                                                if (it.id == complaint.id) it.copy(status = newStatus) else it
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Image Preview Modal Dialog
            previewImageUrl?.let { imgUrl ->
                Dialog(onDismissRequest = { previewImageUrl = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Complaint Photo",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                IconButton(onClick = { previewImageUrl = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Full complaint photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 200.dp, max = 400.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Add Complaint Dialog with Multimedia upload
            if (showDialog) {
                AddComplaintDialog(
                    onDismiss = { showDialog = false },
                    onSubmit = { title, desc, category, photoPath ->
                        showDialog = false
                        scope.launch {
                            try {
                                val userId = SupabaseProvider.auth.currentUserOrNull()?.id ?: ""
                                val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                                val newComplaint = Complaint(
                                    title = title,
                                    description = desc,
                                    category = category,
                                    studentId = userId,
                                    status = "Submitted",
                                    photoUrl = photoPath,
                                    createdAt = timestamp
                                )
                                SupabaseProvider.db.from("complaints").insert(newComplaint)
                                fetchComplaints()
                            } catch (_: Exception) {
                                complaints = listOf(
                                    Complaint(
                                        id = UUID.randomUUID().toString(),
                                        title = title,
                                        description = desc,
                                        category = category,
                                        status = "Submitted",
                                        photoUrl = photoPath,
                                        createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                                    )
                                ) + complaints
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ComplaintCard(
    complaint: Complaint,
    isAdmin: Boolean = false,
    onImageClick: (String) -> Unit = {},
    onStatusUpdate: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = complaint.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Category: ${complaint.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentTeal
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = complaint.status)
            }

            // Description
            complaint.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }

            // Multimedia Photo Section
            complaint.photoUrl?.takeIf { it.isNotBlank() }?.let { url ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onImageClick(url) },
                    color = DarkCardSurfaceVariant
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Complaint problem photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ZoomIn,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Photo", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Admin / Warden Resolution Controls
            if (isAdmin) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = DarkCardSurfaceVariant
                )
                Text(
                    text = "Update Status:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Assigned", "In Progress", "Resolved").forEach { statusOption ->
                        val isCurrent = complaint.status.equals(statusOption, ignoreCase = true)
                        OutlinedButton(
                            onClick = { onStatusUpdate(statusOption) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isCurrent) AccentBlueLight.copy(alpha = 0.2f) else Color.Transparent,
                                contentColor = if (isCurrent) AccentBlueLight else TextMuted
                            ),
                            border = BorderStroke(1.dp, if (isCurrent) AccentBlueLight else DarkCardSurfaceVariant),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (statusOption == "Resolved") "Resolved ✓" else statusOption,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "resolved" -> Pair(StatusGreen.copy(alpha = 0.2f), StatusGreen)
        "in progress" -> Pair(AccentAmber.copy(alpha = 0.2f), AccentAmber)
        "assigned" -> Pair(AccentBlueLight.copy(alpha = 0.2f), AccentBlueLight)
        else -> Pair(StatusRed.copy(alpha = 0.2f), StatusRed)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComplaintDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, desc: String, category: String, photoPath: String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Electrical") }
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }

    // Multimedia state
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessingPhoto by remember { mutableStateOf(false) }

    val categoryList = listOf("Electrical", "Plumbing", "AC", "Furniture", "Cleanliness", "Internet", "Other")

    // Helper to persist bitmap locally and return file URI
    suspend fun saveBitmapToFile(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val fileName = "complaint_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        file.absolutePath
    }

    // Helper to load Uri to Bitmap
    suspend fun uriToBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            photoUri = tempCameraUri
            scope.launch {
                isProcessingPhoto = true
                capturedBitmap = uriToBitmap(tempCameraUri!!)
                isProcessingPhoto = false
            }
        }
    }

    // Gallery / Photo Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
            scope.launch {
                isProcessingPhoto = true
                capturedBitmap = uriToBitmap(uri)
                isProcessingPhoto = false
            }
        }
    }

    fun prepareCameraAndLaunch() {
        try {
            val photoFile = File.createTempFile(
                "CAMERA_${System.currentTimeMillis()}_",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (_: Exception) {
            // Fallback to gallery picker if camera URI initialization encounters issues
            galleryLauncher.launch("image/*")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Maintenance Complaint",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Broken Water Tap)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection
                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownOpen,
                    onExpandedChange = { isCategoryDropdownOpen = !isCategoryDropdownOpen }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownOpen) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownOpen,
                        onDismissRequest = { isCategoryDropdownOpen = false }
                    ) {
                        categoryList.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    isCategoryDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Room Details") },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // MULTIMEDIA SECTION
                Text(
                    text = "Photo of the Issue (Multimedia)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                if (capturedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, AccentTeal, RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = {
                                capturedBitmap = null
                                photoUri = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DarkCardSurfaceVariant, RoundedCornerShape(12.dp)),
                        color = DarkCardSurface
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isProcessingPhoto) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentBlueLight)
                            } else {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = AccentBlueLight,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Attach photo to help maintenance diagnose faster",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { prepareCameraAndLaunch() },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardSurfaceVariant)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Camera", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                                    }
                                    Button(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardSurfaceVariant)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Gallery", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        scope.launch {
                            var savedPath: String? = null
                            if (capturedBitmap != null) {
                                savedPath = saveBitmapToFile(capturedBitmap!!)
                            }
                            onSubmit(title.trim(), desc.trim(), selectedCategory, savedPath)
                        }
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlueLight)
            ) {
                Text("Submit Complaint", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = DarkCardSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
