package com.example.fitflow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.fitflow.R

// Single brand font across the entire app (including digits) to avoid mixed typography.
val AppFontFamily = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold),
    Font(R.font.barlow_black, FontWeight.Black),
    Font(R.font.barlow_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.barlow_blackitalic, FontWeight.Black, FontStyle.Italic)
)

private val BaseTypography = Typography()

val Typography = Typography(
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = AppFontFamily),

    // Screen Title
    displayLarge = BaseTypography.displayLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Italic,
        fontSize = 72.sp,
        lineHeight = 72.sp,
        letterSpacing = 0.15.em
    ),

    displayMedium = BaseTypography.displayMedium.copy(fontFamily = AppFontFamily),

    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = AppFontFamily),

    // Card Heading
    headlineMedium = BaseTypography.headlineMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        fontSize = 28.sp,
        letterSpacing = 0.03.em
    ),

    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = AppFontFamily),

    titleMedium = BaseTypography.titleMedium.copy(fontFamily = AppFontFamily),

    titleSmall = BaseTypography.titleSmall.copy(fontFamily = AppFontFamily),

    // Cyan Emphasis
    titleLarge = BaseTypography.titleLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Italic,
        fontSize = 20.sp
    ),

    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = AppFontFamily),

    // Body Text
    bodyMedium = BaseTypography.bodyMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = TextSecondary,
        letterSpacing = 0.02.em
    ),

    bodySmall = BaseTypography.bodySmall.copy(fontFamily = AppFontFamily),

    labelLarge = BaseTypography.labelLarge.copy(fontFamily = AppFontFamily),

    // Section Label
    labelSmall = BaseTypography.labelSmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.35.em,
        color = TextSecondary
    ),

    // Tag / Badge
    labelMedium = BaseTypography.labelMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.15.em
    )
)