package com.example.fitflow


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitflow.ui.components.BottomNavbar
import com.example.fitflow.ui.screens.DashboardScreen
import com.example.fitflow.ui.screens.LibraryScreen
import com.example.fitflow.ui.screens.WorkoutSetupScreen
//import com.example.fitflow.ui.screens.LibraryScreen
import com.example.fitflow.ui.screens.PlannerScreen
import com.example.fitflow.ui.screens.WorkoutDayDetailScreen
import com.example.fitflow.ui.screens.ProfileScreen
import com.example.fitflow.ui.screens.OnboardingScreen
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.viewmodel.UserViewModel
import com.example.fitflow.viewmodel.UserViewModelFactory


//import com.example.fitflow.ui.screens.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as FitFlowApplication
        val userPreferences = app.userPreferences
        val viewModel: UserViewModel by viewModels {
            UserViewModelFactory(userPreferences)
        }
        val startDestination = if (userPreferences.isOnboarded()) "dashboard" else "onboarding"

        setContent {
            FitflowTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

                Scaffold(
                    bottomBar = {
                        val hideNav = currentRoute == "onboarding"
                            || currentRoute == "workout_setup"
                            || (currentRoute?.startsWith("day_detail") == true)
                        if (!hideNav) {
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
                        startDestination = startDestination,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("dashboard") { DashboardScreen() }
                        composable("planner") {
                            val workoutPlan   by viewModel.workoutPlan.collectAsState()
                            val completedDays by viewModel.completedDays.collectAsState()
                            // Ngày tiếp theo cần tập = ngày workout đầu tiên chưa hoàn thành
                            val currentDay = workoutPlan
                                .filter { !it.isRest }
                                .firstOrNull { it.dayNumber !in completedDays }
                                ?.dayNumber ?: -1
                            PlannerScreen(
                                workoutPlan   = workoutPlan,
                                completedDays = completedDays,
                                currentDay    = currentDay,
                                onDayClick    = { dayNumber ->
                                    navController.navigate("day_detail/$dayNumber")
                                }
                            )
                        }
                        composable(
                            route = "day_detail/{dayNumber}",
                            arguments = listOf(navArgument("dayNumber") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val dayNumber   = backStackEntry.arguments?.getInt("dayNumber") ?: return@composable
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val dayPlan     = workoutPlan.find { it.dayNumber == dayNumber } ?: return@composable
                            WorkoutDayDetailScreen(
                                dayPlan       = dayPlan,
                                onBack        = { navController.popBackStack() },
                                onDayComplete = {
                                    viewModel.markDayComplete(dayNumber)
                                    navController.popBackStack()
                                }
                            )
                        }
                        //composable("library") { LibraryScreen() }
                        composable("profile") {
                            ProfileScreen(onReCalibrate = {
                                navController.navigate("onboarding")
                            })
                        }
                        composable("onboarding") {
                            com.example.fitflow.ui.screens.OnboardingScreen(onComplete = { height, weight ->
                                viewModel.saveProfile(height, weight)
                                navController.navigate("workout_setup")
                            })
                        }
                        composable("library") {
                            LibraryScreen()
                        }
                        composable("workout_setup") {
                            com.example.fitflow.ui.screens.WorkoutSetupScreen(onComplete = {
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

