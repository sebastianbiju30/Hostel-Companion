package com.example.hostel_companion.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hostel_companion.data.SupabaseProvider
import com.example.hostel_companion.data.models.UserProfile
import com.example.hostel_companion.ui.components.BottomNavBar
import com.example.hostel_companion.ui.screens.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun HostelNavGraph() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val currentUser = SupabaseProvider.auth.currentUserOrNull()

    var initialRoute by remember { mutableStateOf("auth") }
    var isCheckingRole by remember { mutableStateOf(currentUser != null) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            scope.launch {
                try {
                    val profile = SupabaseProvider.db.from("profiles")
                        .select { filter { eq("id", currentUser.id) } }
                        .decodeSingleOrNull<UserProfile>()
                    val isWarden = profile?.role?.lowercase() in listOf("admin", "warden") || profile?.isAdmin == true
                    initialRoute = if (isWarden) "admin_dashboard" else "dashboard"
                } catch (_: Exception) {
                    initialRoute = "dashboard"
                } finally {
                    isCheckingRole = false
                }
            }
        } else {
            initialRoute = "auth"
            isCheckingRole = false
        }
    }

    if (isCheckingRole) {
        // Brief loading indicator while resolving role
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show student bottom bar only on student views, NEVER on admin dashboard or auth
    val showBottomBar = currentRoute in listOf("dashboard", "notices", "complaints", "events", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(
                    onLoginSuccess = { isNewUser, role ->
                        val target = when {
                            role in listOf("admin", "warden") -> "admin_dashboard"
                            isNewUser -> "onboarding"
                            else -> "dashboard"
                        }
                        navController.navigate(target) {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            composable("onboarding") {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate("dashboard") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            composable("admin_dashboard") {
                AdminDashboardScreen(navController = navController)
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
            composable("chatbot") {
                ChatBotScreen(navController = navController)
            }
            composable("profile") {
                ProfileScreen(
                    navController = navController,
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
