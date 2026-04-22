package com.example.fitflow


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitflow.ui.components.BottomNavbar
import com.example.fitflow.ui.screens.DashboardScreen
//import com.example.fitflow.ui.screens.LibraryScreen
import com.example.fitflow.ui.screens.PlannerScreen
import com.example.fitflow.ui.screens.ProfileScreen
import com.example.fitflow.ui.theme.FitflowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitflowTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

                Scaffold(
                    bottomBar = {
                        if (currentRoute != "onboarding") {
                            BottomNavbar(currentRoute) { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("dashboard") { DashboardScreen() }
                        composable("planner") { PlannerScreen() }
                        //composable("library") { LibraryScreen() }
                        composable("profile") {
                            ProfileScreen(onReCalibrate = {
                                navController.navigate("onboarding")
                            })
                        }
                        composable("onboarding") {
                            com.fitflow.ui.screens.OnboardingScreen(onComplete = {
                                navController.navigate("workout_setup")
                            })
                        }
                        composable("workout_setup") {
                            com.fitflow.ui.screens.WorkoutSetupScreen(onComplete = {
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

