package com.example.fitflow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitflow.data.PlanRepository
import com.example.fitflow.ui.components.BottomNavbar
import com.example.fitflow.ui.screens.DashboardScreen
import com.example.fitflow.ui.screens.LibraryScreen
import com.example.fitflow.ui.screens.LoadingScreen
import com.example.fitflow.ui.screens.WorkoutSetupScreen
import com.example.fitflow.ui.screens.PlannerScreen
import com.example.fitflow.ui.screens.WorkoutDayDetailScreen
import com.example.fitflow.ui.screens.RestDayDetailScreen
import com.example.fitflow.ui.screens.WorkoutSessionScreen
import com.example.fitflow.ui.screens.ProfileScreen
import com.example.fitflow.ui.screens.OnboardingScreen
import com.example.fitflow.ui.screens.EditPlanScreen
import com.example.fitflow.ui.screens.DayWorkoutSummaryScreen
import com.example.fitflow.ui.screens.WorkoutCompletedScreen
import com.example.fitflow.ui.screens.WorkoutHistoryScreen
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.viewmodel.UserViewModel
import com.example.fitflow.viewmodel.UserViewModelFactory
import com.example.fitflow.viewmodel.WorkoutPlannerViewModel
import com.example.fitflow.ui.screens.WorkoutSettingsScreen
import com.example.fitflow.viewmodel.WorkoutSettingsViewModel
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import java.time.LocalDate
import androidx.compose.runtime.LaunchedEffect
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.data.model.WorkoutExercise
import com.example.fitflow.notification.FitnessNotificationService

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("FitFlowDebug", "Notification permission granted: $isGranted")
        if (isGranted) {
            startFitnessService()
        }
    }

    private val requestActivityRecognitionPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("FitFlowDebug", "Activity recognition permission granted: $isGranted")
        uiViewModel?.setActivityRecognitionGranted(isGranted)
        if (isGranted) {
            uiViewModel?.startStepTracking(applicationContext)
        }
    }

    private var uiViewModel: UserViewModel? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FitnessNotificationService.CHANNEL_ID,
                "Fitness Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Trạng thái tập luyện và nước uống"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startFitnessService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, FitnessNotificationService::class.java)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        val app = application as FitFlowApplication
        val viewModel: UserViewModel by viewModels {
            UserViewModelFactory(app.userPreferences, app.exerciseRepository, PlanRepository(applicationContext))
        }
        val plannerViewModel: WorkoutPlannerViewModel by viewModels()
        uiViewModel = viewModel
        val startDestination = if (app.userPreferences.isOnboarded()) "dashboard" else "onboarding"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startFitnessService()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            viewModel.setActivityRecognitionGranted(granted)
            if (granted) {
                viewModel.startStepTracking(applicationContext)
            }
        } else {
            viewModel.setActivityRecognitionGranted(true)
            viewModel.startStepTracking(applicationContext)
        }

        setContent {
            FitflowTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"
                val settingsViewModel: WorkoutSettingsViewModel by viewModels()

                Scaffold(
                    bottomBar = {
                        val hideNav = currentRoute == "onboarding"
                                || currentRoute == "workout_setup"
                                || currentRoute == "loading"
                                || currentRoute == "update_goal"
                                || (currentRoute.startsWith("day_detail"))
                                || (currentRoute.startsWith("workout_session"))
                                || (currentRoute.startsWith("workout_completed"))
                            || (currentRoute.startsWith("day_workout_summary"))
                                || (currentRoute.startsWith("edit_plan"))
                                || (currentRoute.startsWith("workout_settings"))
                                || currentRoute == "history"
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(paddingValues)
                    ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = {
                            slideInHorizontally(initialOffsetX = { it }) + fadeIn()
                        },
                        exitTransition = {
                            slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut()
                        },
                        popEnterTransition = {
                            slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn()
                        },
                        popExitTransition = {
                            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                        }
                    ) {
                        composable(
                            route = "dashboard",
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None },
                            popEnterTransition = { EnterTransition.None },
                            popExitTransition = { ExitTransition.None }
                        ) {
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val completedDays by viewModel.completedDays.collectAsState()
                            val completedDateMap by viewModel.completedDateMap.collectAsState()
                            val userProfile by viewModel.userProfile.collectAsState()
                            val startDate by viewModel.startDate.collectAsState()
                            val todayHealthMetrics by viewModel.todayHealthMetrics.collectAsState()
                            val activityGranted by viewModel.activityRecognitionGranted.collectAsState()
                            val stepSensorEnabled by viewModel.stepSensorEnabled.collectAsState()
                            val stepTrackingActive by viewModel.stepTrackingActive.collectAsState()
                            val currentStreak by viewModel.currentStreak.collectAsState()

                            LaunchedEffect(workoutPlan, completedDays, todayHealthMetrics) {
                                val nextDay = workoutPlan.firstOrNull { it.dayNumber !in completedDays }

                                if (nextDay != null) {
                                    FitnessNotificationService.currentDay    = nextDay.dayNumber
                                    FitnessNotificationService.totalDays     = workoutPlan.size
                                    FitnessNotificationService.challengeName = nextDay.title
                                    FitnessNotificationService.challengeState = when {
                                        nextDay.isRest                       -> "rest"
                                        nextDay.dayNumber in completedDays   -> "done"
                                        else                                 -> "todo"
                                    }
                                }

                                FitnessNotificationService.waterCurrent = todayHealthMetrics.waterIntakeMl
                                FitnessNotificationService.waterGoal    = todayHealthMetrics.waterGoalMl
                                FitnessNotificationService.refresh(this@MainActivity)
                            }

                            DashboardScreen(
                                completedDays = completedDays,
                                completedDateMap = completedDateMap,
                                currentStreak = currentStreak,
                                workoutPlan = workoutPlan,
                                userProfile = userProfile,
                                startDate = startDate,
                                healthMetrics = todayHealthMetrics,
                                isActivityRecognitionGranted = activityGranted,
                                isStepSensorEnabled = stepSensorEnabled,
                                isStepTrackingActive = stepTrackingActive,
                                onUnlockStepSensor = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        requestActivityRecognitionPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
                                    }
                                },
                                onAddWater = { amount ->
                                    viewModel.addWater(amount)
                                    FitnessNotificationService.waterCurrent =
                                        (FitnessNotificationService.waterCurrent + amount)
                                            .coerceAtMost(FitnessNotificationService.waterGoal)
                                    FitnessNotificationService.refresh(this@MainActivity)
                                             },
                                onSetWaterGoal = { goal -> viewModel.setWaterGoal(goal) },
                                onSetStepGoal = { goal -> viewModel.setStepGoal(goal) },
                                onStartWorkout = {
                                    navController.navigate("planner") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onOpenChatbot = {  },
                                onOpenPlanner = {
                                    navController.navigate("planner") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onOpenSupplementary = { id ->
                                    navController.navigate("supplementary_detail/$id")
                                },
                                onOpenDaySummary = { dayNumber, epochDay ->
                                    navController.navigate("day_workout_summary/$dayNumber?date=$epochDay")
                                },
                                onOpenHistory = {
                                    navController.navigate("history")
                                },
                                onOpenChangeGoal = {
                                    navController.navigate("update_goal")
                                }
                            )
                        }
                        composable("history") {
                            val completedDays by viewModel.completedDays.collectAsState()
                            val completedDateMap by viewModel.completedDateMap.collectAsState()
                            val workoutTimestamps by viewModel.workoutTimestamps.collectAsState()
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            WorkoutHistoryScreen(
                                completedDays = completedDays,
                                completedDateMap = completedDateMap,
                                workoutTimestamps = workoutTimestamps,
                                workoutPlan = workoutPlan,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "planner",
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None },
                            popEnterTransition = { EnterTransition.None },
                            popExitTransition = { ExitTransition.None }
                        ) {
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
                                onDayClick = { dayNumber, isRest ->
                                    if (isRest) {
                                        navController.navigate("rest_day_detail/$dayNumber")
                                    } else {
                                        navController.navigate("day_detail/$dayNumber")
                                    }
                                },
                                onOpenSettings = { navController.navigate("workout_settings") },
                                onResetPlan = { viewModel.resetPlan() }
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
                                onStartSession = { navController.navigate("workout_session/$dayNumber") },
                                onEditPlan = { navController.navigate("edit_plan/$dayNumber") },
                                onOpenSettings = { navController.navigate("workout_settings") }
                            )
                        }
                        composable(
                            route = "rest_day_detail/{dayNumber}",
                            arguments = listOf(navArgument("dayNumber") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val dayNumber = backStackEntry.arguments?.getInt("dayNumber") ?: return@composable
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val dayPlan = workoutPlan.find { it.dayNumber == dayNumber } ?: return@composable
                            RestDayDetailScreen(
                                dayPlan = dayPlan,
                                onBack = { navController.popBackStack() },
                                onMarkRestComplete = {
                                    viewModel.markDayComplete(dayNumber)
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = "workout_settings?inSession={inSession}",
                            arguments = listOf(navArgument("inSession") {
                                type = NavType.BoolType
                                defaultValue = false
                            })
                        ) { backStackEntry ->
                            val inSession = backStackEntry.arguments?.getBoolean("inSession") ?: false
                            WorkoutSettingsScreen(
                                onBack = { navController.popBackStack() },
                                viewModel = settingsViewModel,
                                isInWorkoutSession = inSession
                            )
                        }
                        composable(
                            route = "edit_plan/{dayNumber}",
                            arguments = listOf(navArgument("dayNumber") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val dayNumber =
                                backStackEntry.arguments?.getInt("dayNumber") ?: return@composable
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val dayPlan =
                                workoutPlan.find { it.dayNumber == dayNumber } ?: return@composable
                            val allExercises by viewModel.allExercises.collectAsState()

                            EditPlanScreen(
                                dayNumber = dayNumber,
                                dayTitle = dayPlan.title,
                                initialExercises = dayPlan.workoutExercises,
                                allDatabaseExercises = allExercises,
                                plannerViewModel = plannerViewModel,
                                onBack = { navController.popBackStack() },
                                onSave = { updatedExercises ->
                                    viewModel.updateDayPlan(dayNumber, updatedExercises)
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = "profile",
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None },
                            popEnterTransition = { EnterTransition.None },
                            popExitTransition = { ExitTransition.None }
                        ) {
                            val userProfile by viewModel.userProfile.collectAsState()
                            val completedDays by viewModel.completedDays.collectAsState()
                            val completedDateMap by viewModel.completedDateMap.collectAsState()
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val startDate by viewModel.startDate.collectAsState()
                            val weightHistory by viewModel.weightHistory.collectAsState()
                            val healthHistory by viewModel.healthMetricsHistory.collectAsState()
                            val todayHealthMetrics by viewModel.todayHealthMetrics.collectAsState()
                            val activityGranted by viewModel.activityRecognitionGranted.collectAsState()
                            val stepSensorEnabled by viewModel.stepSensorEnabled.collectAsState()
                            val stepTrackingActive by viewModel.stepTrackingActive.collectAsState()
                            ProfileScreen(
                                userProfile = userProfile,
                                completedDays = completedDays,
                                workoutPlan = workoutPlan,
                                startDate = startDate,
                                completedDateMap = completedDateMap,
                                weightHistory = weightHistory,
                                healthMetricsHistory = healthHistory,
                                todayHealthMetrics = todayHealthMetrics,
                                isActivityRecognitionGranted = activityGranted,
                                isStepSensorEnabled = stepSensorEnabled,
                                isStepTrackingActive = stepTrackingActive,
                                onRecordWeight = { viewModel.recordWeight(it) },
                                onReCalibrate = { navController.navigate("onboarding") },
                                onUnlockStepSensor = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        requestActivityRecognitionPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
                                    }
                                },
                                onAddWater = { amount -> viewModel.addWater(amount) },
                                onSetWaterGoal = { goal -> viewModel.setWaterGoal(goal) },
                                onSetStepGoal = { goal -> viewModel.setStepGoal(goal) }
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
                        composable(
                            route = "library",
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None },
                            popEnterTransition = { EnterTransition.None },
                            popExitTransition = { ExitTransition.None }
                        ) {
                            LibraryScreen()
                        }
                        composable("loading") {
                            val provisioningState by viewModel.planProvisioningState.collectAsState()
                            LoadingScreen(
                                provisioningState = provisioningState,
                                onPlanReady = {
                                    navController.navigate("dashboard") {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onConfirmMobileData = {
                                    viewModel.confirmUseMobileDataForProvisioning(applicationContext)
                                },
                                onRetry = {
                                    viewModel.retryPlanProvisioning(applicationContext)
                                }
                            )
                        }
                        composable("workout_setup") {
                            WorkoutSetupScreen(onComplete = { goal ->
                                viewModel.startPlanProvisioning(applicationContext, goal)
                                navController.navigate("loading") {
                                    popUpTo("workout_setup") { inclusive = true }
                                }
                            })
                        }
                        composable("update_goal") {
                            val userProfile by viewModel.userProfile.collectAsState()
                            if (userProfile != null) {
                                com.example.fitflow.ui.screens.UpdateGoalScreen(
                                    currentGoal = userProfile!!.goal,
                                    onBack = { navController.popBackStack() },
                                    onComplete = { goal, resetProgress ->
                                        if (resetProgress) {
                                            // User chose Day 1 — wipe frozen data for this goal
                                            viewModel.clearProgressForGoal(goal)
                                        }
                                        // startPlanProvisioning saves the new goal and regenerates
                                        // the plan; it no longer touches completedDays itself.
                                        viewModel.startPlanProvisioning(applicationContext, goal)
                                        navController.navigate("loading") {
                                            popUpTo("update_goal") { inclusive = true }
                                        }
                                    }
                                )
                            }
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
                                onBack = {
                                    settingsViewModel.stopMusic()
                                    navController.popBackStack()
                                },
                                onFinish = { activeSeconds ->
                                    settingsViewModel.stopMusic()
                                    viewModel.markDayComplete(dayNumber)
                                    navController.navigate("workout_completed/$dayNumber?activeSeconds=$activeSeconds") {
                                        popUpTo("dashboard") { saveState = true }
                                    }
                                },
                                onOpenSettings = { navController.navigate("workout_settings?inSession=true") },
                                settingsViewModel = settingsViewModel
                            )
                        }
                        composable(
                            route = "workout_completed/{dayNumber}?activeSeconds={activeSeconds}",
                            arguments = listOf(
                                navArgument("dayNumber") { type = NavType.IntType },
                                navArgument("activeSeconds") { 
                                    type = NavType.IntType 
                                    defaultValue = 0
                                }
                            )
                        ) { backStackEntry ->
                            val dayNumber = backStackEntry.arguments?.getInt("dayNumber") ?: return@composable
                            val activeSeconds = backStackEntry.arguments?.getInt("activeSeconds") ?: 0
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val dayPlan = workoutPlan.find { it.dayNumber == dayNumber }
                            val userProfile by viewModel.userProfile.collectAsState()

                            WorkoutCompletedScreen(
                                dayPlan = dayPlan,
                                totalActiveSeconds = activeSeconds,
                                userProfile = userProfile,
                                onSaveWeight = { newWeight -> viewModel.recordWeight(newWeight) },
                                onNext = {
                                    navController.navigate("day_workout_summary/$dayNumber") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable(
                            route = "day_workout_summary/{dayNumber}?date={date}",
                            arguments = listOf(
                                navArgument("dayNumber") { type = NavType.IntType },
                                navArgument("date") { 
                                    type = NavType.LongType 
                                    defaultValue = -1L
                                }
                            )
                        ) { backStackEntry ->
                            val dayNumber =
                                backStackEntry.arguments?.getInt("dayNumber") ?: return@composable
                            val epochDay = backStackEntry.arguments?.getLong("date") ?: -1L
                            val workoutPlan by viewModel.workoutPlan.collectAsState()
                            val completedDays by viewModel.completedDays.collectAsState()
                            val currentStreak by viewModel.currentStreak.collectAsState()
                            val startDate by viewModel.startDate.collectAsState()


                            var selectedDate by remember(dayNumber, startDate, epochDay) {
                                mutableStateOf<LocalDate>(
                                    if (epochDay != -1L) LocalDate.ofEpochDay(epochDay)
                                    else startDate?.plusDays((dayNumber - 1).toLong()) ?: LocalDate.now()
                                )
                            }

                            val completedDateMap by viewModel.completedDateMap.collectAsState()

                            DayWorkoutSummaryScreen(
                                selectedDate = selectedDate,
                                workoutPlan = workoutPlan,
                                completedDays = completedDays,
                                completedDateMap = completedDateMap,
                                currentStreak = currentStreak,
                                startDate = startDate,
                                onBack = {
                                    // Try to pop back to an existing dashboard in the backstack.
                                    val popped = navController.popBackStack("dashboard", false)
                                    if (!popped) {
                                        // If dashboard isn't on the backstack, navigate there.
                                        navController.navigate("dashboard") {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                onNavigateDay = { selectedDate = it },
                                onOpenPlanner = {
                                    navController.navigate("planner") {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onOpenWorkoutDay = { targetDayNumber ->
                                    navController.navigate("day_detail/$targetDayNumber") {
                                        popUpTo("planner")
                                    }
                                }
                            )
                        }
                        composable(
                            route = "supplementary_detail/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: return@composable
                            var dayPlan by remember { mutableStateOf<DayPlan?>(null) }
                            
                            LaunchedEffect(id) {
                                val workout = viewModel.getSupplementaryWorkout(id)
                                dayPlan = workout?.toDayPlan()
                            }

                            if (dayPlan != null) {
                                WorkoutDayDetailScreen(
                                    dayPlan = dayPlan!!,
                                    onBack = { navController.popBackStack() },
                                    onStartSession = {
                                        // Push Your Limits uses the standard workout_session but without a dayNumber.
                                        // Wait, workout_session requires dayNumber.
                                        // I should navigate to supplementary_session/$id
                                        navController.navigate("supplementary_session/$id")
                                    },
                                    onEditPlan = { },
                                    onOpenSettings = { navController.navigate("workout_settings") }
                                )
                            }
                        }
                        composable(
                            route = "supplementary_session/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: return@composable
                            var exercises by remember { mutableStateOf<List<WorkoutExercise>?>(null) }

                            LaunchedEffect(id) {
                                val workout = viewModel.getSupplementaryWorkout(id)
                                exercises = workout?.exercises
                            }

                            if (exercises != null) {
                                WorkoutSessionScreen(
                                    exercises = exercises!!,
                                    onBack = {
                                        settingsViewModel.stopMusic()
                                        navController.popBackStack()
                                    },
                                    onFinish = { activeSeconds ->
                                        settingsViewModel.stopMusic()
                                        navController.popBackStack()
                                    },
                                    onOpenSettings = { navController.navigate("workout_settings?inSession=true") },
                                    settingsViewModel = settingsViewModel
                                )
                            }
                        }
                    } // end NavHost
                    } // end Box
                }
            }
        }
    }

    override fun onDestroy() {
        uiViewModel?.stopStepTracking()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        syncActivityRecognitionPermission()
        uiViewModel?.refreshHealthMetrics()
        if (uiViewModel?.activityRecognitionGranted?.value == true) {
            uiViewModel?.startStepTracking(applicationContext)
        }
    }

    override fun onStop() {
        uiViewModel?.stopStepTracking()
        super.onStop()
    }

    private fun syncActivityRecognitionPermission() {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        uiViewModel?.setActivityRecognitionGranted(granted)
    }
}



