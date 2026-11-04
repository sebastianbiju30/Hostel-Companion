package com.example.hostel_companion.data.local

/**
 * Data model for personal student tasks, checklists, and room notes stored in the local SQLite database.
 */
data class LocalTask(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General", // "Room", "Academic", "Laundry & Mess", "Personal", "General"
    val isCompleted: Boolean = false,
    val createdAt: String = ""
)

