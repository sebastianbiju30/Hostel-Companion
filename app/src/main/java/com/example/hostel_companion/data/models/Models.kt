package com.example.hostel_companion.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String = "student",
    @SerialName("room_id") val roomId: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val isAdmin: Boolean = false
)

@Serializable
data class Room(
    val id: String = "",
    @SerialName("room_number") val roomNumber: String,
    val block: String? = null,
    val capacity: Int = 2,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Complaint(
    val id: String = "",
    @SerialName("student_id") val studentId: String = "",
    val title: String,
    val description: String? = null,
    val category: String = "Other",
    val status: String = "Submitted",
    @SerialName("assigned_to") val assignedTo: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null
)

@Serializable
data class Notice(
    val id: String = "",
    val title: String,
    val body: String? = null,
    val priority: String = "Low",
    @SerialName("posted_by") val postedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Event(
    val id: String = "",
    val title: String,
    val description: String? = null,
    val location: String? = null,
    @SerialName("event_date") val eventDate: String,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class EmergencyContact(
    val id: String = "",
    val label: String,
    val description: String? = null,
    val phone: String,
    val type: String? = null
)

@Serializable
data class MessMenu(
    val id: String = "",
    val day: String,
    val meal: String,
    val items: String? = null,
    val calories: Int? = null,
    @SerialName("serve_time") val serveTime: String? = null
)

@Serializable
data class LaundryOrder(
    val id: String = "",
    @SerialName("student_id") val studentId: String = "",
    @SerialName("items_count") val itemsCount: Int = 1,
    val status: String = "Dropped",
    @SerialName("dropped_at") val droppedAt: String? = null
)

@Serializable
data class Parcel(
    val id: String = "",
    @SerialName("student_id") val studentId: String = "",
    val sender: String? = null,
    val description: String? = null,
    val status: String = "Arrived",
    @SerialName("arrived_at") val arrivedAt: String? = null
)

@Serializable
data class Visitor(
    val id: String = "",
    @SerialName("student_id") val studentId: String = "",
    @SerialName("visitor_name") val visitorName: String,
    @SerialName("visitor_phone") val visitorPhone: String? = null,
    val purpose: String? = null,
    val status: String = "Pending",
    @SerialName("requested_at") val requestedAt: String? = null
)
