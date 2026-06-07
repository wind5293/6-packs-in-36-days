package com.example.fitflow.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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

    val firstProgress by animateFloatAsState(
        targetValue = provisioningState.firstSegmentProgress,
        animationSpec = tween(durationMillis = 300),
        label = "first_progress"
    )

    val secondProgress by animateFloatAsState(
        targetValue = provisioningState.secondSegmentProgress,
        animationSpec = tween(durationMillis = 300),
        label = "second_progress"
    )

    val statusText = when {
        provisioningState.requiresMobileDataConsent -> "Using mobile data may consume data to prepare workouts."
        provisioningState.isNoNetwork -> "Network is required for first-time workout media setup."
        provisioningState.hasError -> provisioningState.statusMessage.ifBlank { "Unable to complete setup." }
        else -> provisioningState.statusMessage
    }

    val actionMode = when {
        provisioningState.requiresMobileDataConsent -> "accept"
        provisioningState.isNoNetwork || provisioningState.hasError -> "retry"
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
                fontStyle = FontStyle.Normal
            )
            Text(
                "FOR YOU",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Normal
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgressTrack(
                progress = firstProgress,
                modifier = Modifier.weight(1f)
            )
            ProgressTrack(
                progress = secondProgress,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "GENERATE WORKOUT",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "GIF",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = statusText,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

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
private fun ProgressTrack(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    FitflowTheme() {
        LoadingScreen(
            provisioningState = PlanProvisioningState(
                firstSegmentProgress = 1f,
                secondSegmentProgress = 0.4f,
                statusMessage = "Finalizing setup...",
                isInProgress = true
            ),
            onPlanReady = {},
            onConfirmMobileData = {},
            onRetry = {}
        )
    }
}
