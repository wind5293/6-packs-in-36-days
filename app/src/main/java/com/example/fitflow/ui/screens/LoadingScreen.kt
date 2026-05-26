package com.example.fitflow.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.ui.theme.FitflowTheme
import com.example.fitflow.viewmodel.PlanProvisioningState

@Composable
fun LoadingScreen(
    provisioningState: PlanProvisioningState,
    onPlanReady: () -> Unit,
    onConfirmMobileData: () -> Unit,
    onRetry: () -> Unit
) {
    LaunchedEffect(provisioningState.isCompleted) {
        if (provisioningState.isCompleted) {
            onPlanReady()
        }
    }

    val overallProgress by animateFloatAsState(
        targetValue = provisioningState.progress,
        animationSpec = tween(durationMillis = 300),
        label = "overall_progress"
    )

    val statusText = when {
        provisioningState.requiresMobileDataConsent -> "Using mobile data may consume data to prepare workouts."
        provisioningState.isNoNetwork -> "Network is required for first-time workout media setup."
        provisioningState.hasError -> "Unable to complete setup."
        else -> provisioningState.statusMessage
    }

    val actionMode = when {
        provisioningState.requiresMobileDataConsent -> "accept"
        provisioningState.isNoNetwork -> "retry"
        else -> "none"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "PICKING THE BEST",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp
        )
        Row {
            Text(
                "EXERCISES ",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            )
            Text(
                "FOR YOU",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        SpotlightProgressTrack(progress = overallProgress)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = statusText,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (actionMode != "none") {
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (actionMode == "accept") {
            Button(
                onClick = onConfirmMobileData,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CONTINUE WITH MOBILE DATA",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        if (actionMode == "retry") {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "RETRY",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun SpotlightProgressTrack(progress: Float, modifier: Modifier = Modifier) {
    val clamped = progress.coerceIn(0f, 1f)
    val animatedPercent by animateIntAsState(
        targetValue = (clamped * 100f).toInt(),
        animationSpec = tween(durationMillis = 220),
        label = "loading_percent"
    )
    val percentText = "$animatedPercent%"

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
    ) {
        val density = LocalDensity.current
        val trackWidthPx = with(density) { maxWidth.toPx() }
        val trackCenterPx = trackWidthPx / 2f
        val fillEndPx = trackWidthPx * clamped
        val percentColor = if (fillEndPx >= trackCenterPx) {
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .fillMaxHeight()
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        Text(
            text = percentText,
            modifier = Modifier.align(Alignment.Center),
            color = percentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    FitflowTheme() {
        LoadingScreen(
            provisioningState = PlanProvisioningState(
                progress = 0.64f,
                statusMessage = "Finalizing setup...",
                isInProgress = true
            ),
            onPlanReady = {},
            onConfirmMobileData = {},
            onRetry = {}
        )
    }
}