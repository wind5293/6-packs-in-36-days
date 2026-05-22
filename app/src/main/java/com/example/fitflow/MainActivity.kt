package com.example.fitflow

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitflow.data.model.Exercise
import com.example.fitflow.ui.components.BottomNavbar
import com.example.fitflow.ui.screens.DashboardScreen
import com.example.fitflow.ui.screens.LibraryScreen
import com.example.fitflow.ui.screens.LoadingScreen
import com.example.fitflow.ui.screens.WorkoutSetupScreen
import com.example.fitflow.ui.screens.PlannerScreen
import com.example.fitflow.ui.screens.WorkoutDayDetailScreen
import com.example.fitflow.ui.screens.WorkoutSessionScreen
import com.example.fitflow.ui.screens.ProfileScreen
import com.example.fitflow.ui.screens.OnboardingScreen
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.viewmodel.UserViewModel
import com.example.fitflow.viewmodel.UserViewModelFactory

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("FitFlowDebug", "Notification permission granted: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as FitFlowApplication
        val userPreferences = app.userPreferences
        val viewModel: UserViewModel by viewModels {
            UserViewModelFactory(userPreferences)
        }
        val startDestination = if (userPreferences.isOnboarded()) "dashboard" else "onboarding"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            FitflowTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

                Scaffold(
                    bottomBar = {
                        val hideNav = currentRoute == "onboarding"
                                || currentRoute == "workout_setup"
                                || currentRoute == "loading"
                                || (currentRoute.startsWith("day_detail"))
                                || (currentRoute.startsWith("workout_session"))
                        if (!hideNav) {
                            BottomNavbar(currentRoute) { route ->
                                navController.navigate(route) {
                                    // Luôn pop về "dashboard" — root thật sự của backstack
                                    popUpTo("dashboard") { saveState = true }
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
                        composable("dashboard") {
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val completedDays by viewModel.completedDays.collectAsState()
                            val userProfile by viewModel.userProfile.collectAsState()
                            DashboardScreen(
                                completedDays = completedDays,
                                workoutPlan = workoutPlan,
                                userProfile = userProfile,
                                onStartWorkout = {
                                    navController.navigate("planner") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable("planner") {
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val completedDays by viewModel.completedDays.collectAsState()
                            // Ngày tiếp theo cần tập = ngày workout đầu tiên chưa hoàn thành
                            val currentDay = workoutPlan
                                .filter { !it.isRest }
                                .firstOrNull { it.dayNumber !in completedDays }
                                ?.dayNumber ?: -1
                            PlannerScreen(
                                workoutPlan = workoutPlan,
                                completedDays = completedDays,
                                currentDay = currentDay,
                                onDayClick = { dayNumber ->
                                    navController.navigate("day_detail/$dayNumber")
                                }
                            )
                        }
                        composable(
                            route = "day_detail/{dayNumber}",
                            arguments = listOf(navArgument("dayNumber") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val dayNumber =
                                backStackEntry.arguments?.getInt("dayNumber") ?: return@composable
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val dayPlan =
                                workoutPlan.find { it.dayNumber == dayNumber } ?: return@composable
                            WorkoutDayDetailScreen(
                                dayPlan = dayPlan,
                                onBack = { navController.popBackStack() },
                                onStartSession = { navController.navigate("workout_session/$dayNumber") }
                            )
                        }
                        composable("profile") {
                            val userProfile by viewModel.userProfile.collectAsState()
                            val completedDays by viewModel.completedDays.collectAsState()
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val startDate by viewModel.startDate.collectAsState()
                            ProfileScreen(
                                userProfile = userProfile,
                                completedDays = completedDays,
                                workoutPlan = workoutPlan,
                                startDate = startDate,
                                onReCalibrate = { navController.navigate("onboarding") }
                            )
                        }
                        composable("onboarding") {
                            OnboardingScreen(onComplete = { selectedGoal, height, weight, birthYear, targetWeight, workoutTime ->
                                viewModel.saveProfile(selectedGoal, height, weight, birthYear, targetWeight, workoutTime)

                                val timeParts = workoutTime.split(":")
                                val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                                val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

                                Log.d("FitFlowDebug", "Workout Time: $workoutTime")
                                Log.d("FitFlowDebug", "Parsed -> Hour: $hour, Minute: $minute")
                                viewModel.scheduleWorkoutReminder(this@MainActivity, hour, minute)

                                navController.navigate("workout_setup")
                            })
                        }
                        composable("library") {
                            LibraryScreen()
                        }
                        composable("loading") {
                            LoadingScreen(onPlanReady = {
                                navController.navigate("dashboard") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            })
                        }
                        composable("workout_setup") {
                            WorkoutSetupScreen(onComplete = { goal ->
                                viewModel.saveGoal(goal)
                                navController.navigate("loading") {
                                    popUpTo("workout_setup") { inclusive = true }
                                }
                            })
                        }
                        composable(
                            route = "workout_session/{dayNumber}",
                            arguments = listOf(navArgument("dayNumber") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val dayNumber =
                                backStackEntry.arguments?.getInt("dayNumber") ?: return@composable
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val dayPlan =
                                workoutPlan.find { it.dayNumber == dayNumber } ?: return@composable
                            
                            WorkoutSessionScreen(
                                exercises = dayPlan.workoutExercises,
                                onBack = { navController.popBackStack() },
                                onFinish = {
                                    viewModel.markDayComplete(dayNumber)
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



