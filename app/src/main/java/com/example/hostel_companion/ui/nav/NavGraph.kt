package com.example.hostel_companion.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.ui.components.BottomNavBar
import com.example.hostel_companion.ui.screens.*

@Composable
fun HostelNavGraph() {
    val navController = rememberNavController()
    val initialRoute = if (SupabaseProvider.auth.currentUserOrNull() != null) "dashboard" else "auth"

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(
                    onLoginSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
                DashboardScreen(navController = navController)
            }
            composable("complaints") {
                ComplaintsScreen()
            }
            composable("notices") {
                NoticesScreen()
            }
            composable("events") {
                EventsScreen()
            }
            composable("room") {
                RoomScreen()
            }
            composable("mess") {
                MessMenuScreen()
            }
            composable("laundry") {
                LaundryScreen()
            }
            composable("parcels") {
                ParcelsScreen()
            }
            composable("visitors") {
                VisitorsScreen()
            }
            composable("profile") {
                ProfileScreen(
                    onSignOut = {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
