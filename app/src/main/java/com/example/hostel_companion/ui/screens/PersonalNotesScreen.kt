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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hostel_companion.data.local.HostelLocalDbHelper
import com.example.hostel_companion.data.local.LocalTask
import com.example.hostel_companion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalNotesScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val dbHelper = remember { HostelLocalDbHelper.getInstance(context) }

    var tasks by remember { mutableStateOf<List<LocalTask>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    fun refreshTasks() {
        tasks = dbHelper.getAllTasks()
    }

    LaunchedEffect(Unit) {
        refreshTasks()
    }

    val categories = listOf("All", "Room", "Laundry", "Mess", "Academic", "Personal")

    val filteredTasks = tasks.filter {
        if (selectedCategory == "All") true
        else it.category.equals(selectedCategory, ignoreCase = true)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Personal Room Notes & Tasks",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Stored locally on your device (Offline SQLite)",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentTeal
                        )
                    }
                },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCardSurface),
                actions = {
                    IconButton(onClick = { refreshTasks() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentTeal,
                contentColor = TextPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Note / Task", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Bar
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
                                selectedContainerColor = AccentTeal,
                                selectedLabelColor = TextPrimary,
                                containerColor = DarkCardSurfaceVariant,
                                labelColor = TextMuted
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task Statistics Overview Banner
            val completedCount = tasks.count { it.isCompleted }
            val totalCount = tasks.size
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = DarkCardSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Checklist Progress",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "$completedCount of $totalCount tasks completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    Surface(
                        color = if (completedCount == totalCount && totalCount > 0) StatusGreen.copy(alpha = 0.2f) else AccentTeal.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (totalCount == 0) "No Tasks" else "${(completedCount * 100) / totalCount}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (completedCount == totalCount && totalCount > 0) StatusGreen else AccentTeal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (filteredTasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(52.dp)
                            )
                            Text(
                                text = "No notes or tasks in this category",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Tap '+ Add Note / Task' below to write down your reminders.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (task.isCompleted) DarkCardSurface.copy(alpha = 0.6f) else DarkCardSurface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { isChecked ->
                                            dbHelper.toggleTask(task.id, isChecked)
                                            refreshTasks()
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = StatusGreen,
                                            uncheckedColor = TextMuted,
                                            checkmarkColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                            ),
                                            color = if (task.isCompleted) TextMuted else TextPrimary
                                        )
                                        if (task.description.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = task.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                color = DarkCardSurfaceVariant,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = task.category,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = AccentTeal,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            if (task.createdAt.isNotBlank()) {
                                                Text(
                                                    text = task.createdAt,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            dbHelper.deleteTask(task.id)
                                            refreshTasks()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = StatusRed.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
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

    // Add Task Dialog
    if (showAddDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var taskCategory by remember { mutableStateOf("Room") }
        val dialogCategories = listOf("Room", "Laundry", "Mess", "Academic", "Personal")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Local Note / Reminder",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Title (e.g. Return Library Book)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = DarkCardSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Details / Note (Optional)") },
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = DarkCardSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Category:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(dialogCategories) { cat ->
                            val isSel = taskCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { taskCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentTeal,
                                    selectedLabelColor = TextPrimary,
                                    containerColor = DarkCardSurfaceVariant,
                                    labelColor = TextMuted
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            dbHelper.insertTask(
                                LocalTask(
                                    title = taskTitle.trim(),
                                    description = taskDesc.trim(),
                                    category = taskCategory
                                )
                            )
                            refreshTasks()
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    enabled = taskTitle.isNotBlank()
                ) {
                    Text("Save to Device", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkCardSurface
        )
    }
}
