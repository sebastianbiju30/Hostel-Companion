package com.example.hostel_companion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.hostel_companion.ui.theme.DarkCardSurface
import com.example.hostel_companion.ui.theme.TextMuted
import com.example.hostel_companion.ui.theme.TextPrimary

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("dashboard", "Home", Icons.Default.Home)
    object Notices : BottomNavItem("notices", "Notices", Icons.Default.Assignment)
    object Complaints : BottomNavItem("complaints", "Complaints", Icons.Default.Warning)
    object Events : BottomNavItem("events", "Events", Icons.Default.DateRange)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Notices,
        BottomNavItem.Complaints,
        BottomNavItem.Events,
        BottomNavItem.Profile
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    if (currentRoute in items.map { it.route }) {
        NavigationBar(
            containerColor = DarkCardSurface,
            tonalElevation = 8.dp
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                    selected = isSelected,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextPrimary,
                        unselectedIconColor = TextMuted,
                        selectedTextColor = TextPrimary,
                        unselectedTextColor = TextMuted,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}
