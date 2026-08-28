package com.example.hostel_companion.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    @SerialName("full_name") val fullName: String = "",
    val email: String = "",
    @SerialName("room_number") val roomNumber: String = "",
    val phone: String = "",
    val role: String = "student"
)

@Serializable
data class Complaint(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val title: String,
    val description: String,
    val category: String,
    val status: String = "Pending",
    @SerialName("room_number") val roomNumber: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class Notice(
    val id: String = "",
    val title: String,
    val content: String,
    val category: String = "General",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class Event(
    val id: String = "",
    val title: String,
    val description: String,
    val date: String,
    val location: String = "Hostel Common Room"
)
