# FitFlow Android Project

FitFlow for Android is a high-performance, native mobile application built with Jetpack Compose. It translates the FitFlow fitness protocol into a mobile-first experience, focusing on intensity, tracking.

## Project Overview

The Android version of FitFlow is designed to be the primary portable interface for the system. It maintains functional and visual parity with the web version while utilizing native Android components for a smoother, high-frame-rate experience.

## Key Features

### Physical Calibration and Onboarding
- Native input modules for height and mass density.
- Real-time BMI calculation and health categorization.
- Objective-based goal selection (Weight Loss, Muscle Gain, Endurance, Maintenance).

### Workout Protocol Setup
- Equipment level selection (Bodyweight, Minimalist, Full Protocol).
- Target muscle group focus selection.
- Adjustable weekly frequency via custom-styled sliders.

### Monthly Timeline and Planning
- 30-Day schedule presented in a weekly-segmented list format.
- Direct visibility of scheduled exercises for each day.
- Dynamic highlighting of the current active day.

### Dynamic Health Dashboard
- Performance tracking for steps and water intake.
- Visual progress bars for fitness goals.
- Streak cycle tracking for consistent activity.

## Technical Stack

- Language: Kotlin
- UI Framework: Jetpack Compose
- Component Library: Material 3 (Material Design 3)
- Navigation: Navigation Compose
- Architecture: MVVM (Model-View-ViewModel) pattern
- Minimum SDK: API 26 (Android 8.0 Oreo)
- Target SDK: API 34 (Android 14)

## Design System: Hyper Energy Theme

The application uses a specific high-intensity design system defined in the UI package:
- Primary Color (Hyper Orange): #FF5F07 - Used for actions and intensity indicators.
- Secondary Color (Electric Cyan): #00E5FF - Used for vital metrics and recovery states.
- Background (Surface 0): #0A0B10 - Deep high-contrast dark mode foundation.
- Typography: Focuses on Bold, Black, and Italic weights for a high-performance aesthetic.

## Project Structure

- com.fitflow.MainActivity: Entry point and navigation host controller.
- com.fitflow.ui.theme: Design system definitions including Colors, Typography, and Shapes.
- com.fitflow.ui.screens: Composable screens (Dashboard, Onboarding, Setup, Profile, Planner, Library).
- com.fitflow.ui.components: Reusable UI widgets like BottomNavbar and MetricCards.

## Setup Instructions

1. Open Android Studio (Brave Badger variant or newer recommended).
2. Select Open and navigate to the android_project directory.
3. Allow Gradle to synchronize dependencies.
4. Deploy to a physical device or emulator running at least Android 8.0.

## Development Guidelines

- Ensure all new screens are added to the navigation graph in MainActivity.kt.
- Maintain the Hyper Energy color palette for all new custom components.
- Use LazyColumn or LazyVerticalGrid for all list-based layouts to ensure performance.
