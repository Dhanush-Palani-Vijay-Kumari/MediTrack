package com.meditrack.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.meditrack.ui.appointments.AppointmentsScreen
import com.meditrack.ui.appointments.AddAppointmentScreen
import com.meditrack.ui.home.HomeScreen
import com.meditrack.ui.medications.MedicationsScreen
import com.meditrack.ui.medications.MedicationDetailScreen
import com.meditrack.ui.medications.AddMedicationScreen
import com.meditrack.ui.profile.ProfileScreen
import com.meditrack.ui.vitals.VitalsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home        : Screen("home", "Home", Icons.Outlined.Home)
    object Medications : Screen("medications", "Meds", Icons.Outlined.Medication)
    object Vitals      : Screen("vitals", "Vitals", Icons.Outlined.MonitorHeart)
    object Appointments: Screen("appointments", "Appts", Icons.Outlined.CalendarMonth)
    object Profile     : Screen("profile", "Profile", Icons.Outlined.Person)
}

val bottomNavItems = listOf(
    Screen.Home, Screen.Medications, Screen.Vitals, Screen.Appointments, Screen.Profile
)

@Composable
fun MediTrackApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = bottomNavItems.any {
                currentDestination?.hierarchy?.any { d -> d.route == it.route } == true
            }
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Medications.route) {
                MedicationsScreen(navController = navController)
            }
            composable(
                route = "medication_detail/{medId}",
                arguments = listOf(navArgument("medId") { type = NavType.LongType })
            ) { backStack ->
                MedicationDetailScreen(
                    medId = backStack.arguments?.getLong("medId") ?: 0L,
                    navController = navController
                )
            }
            composable("add_medication") {
                AddMedicationScreen(navController = navController)
            }
            composable(Screen.Vitals.route) {
                VitalsScreen()
            }
            composable(Screen.Appointments.route) {
                AppointmentsScreen(navController = navController)
            }
            composable("add_appointment") {
                AddAppointmentScreen(navController = navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
