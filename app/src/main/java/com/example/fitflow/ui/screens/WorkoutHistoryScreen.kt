package com.example.fitflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitflow.R
import com.example.fitflow.data.model.DayPlan
import com.example.fitflow.ui.theme.FitflowTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// ─── Data model for a completed workout entry ────────────────────────────────
data class WorkoutHistoryEntry(
    val dayPlan: DayPlan,
    val completedDate: LocalDate,
    val durationSeconds: Int = 0,
    val completedAtMillis: Long = 0L
)

// ─── Screen ───────────────────────────────────────────────────────────────────
@Composable
fun WorkoutHistoryScreen(
    completedDays: Set<Int> = emptySet(),
    completedDateMap: Map<LocalDate, Int> = emptyMap(),
    workoutTimestamps: Map<Int, Long> = emptyMap(),    // dayNumber -> epochMillis
    workoutPlan: List<DayPlan> = emptyList(),
    onBack: () -> Unit = {}
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }

    // Build reverse lookup: dayNumber -> date (from completedDateMap)
    // completedDateMap maps date->dayNumber, but multiple days completed on same date
    // means only the last one is stored. We use completedDays (Set<Int>) as source of truth.
    val dayNumberToDate: Map<Int, LocalDate> = remember(completedDateMap) {
        completedDateMap.entries.associate { (date, dayNum) -> dayNum to date }
    }

    // Build history entries from completedDays (source of truth for which days are done)
    val historyEntries: List<WorkoutHistoryEntry> = remember(completedDays, dayNumberToDate, workoutTimestamps, workoutPlan) {
        completedDays
            .sortedDescending()
            .mapNotNull { dayNumber ->
                val plan = workoutPlan.find { it.dayNumber == dayNumber } ?: return@mapNotNull null
                val date = dayNumberToDate[dayNumber] ?: LocalDate.now()
                val millis = workoutTimestamps[dayNumber] ?: 0L
                WorkoutHistoryEntry(
                    dayPlan = plan,
                    completedDate = date,
                    durationSeconds = plan.workoutExercises.sumOf { it.durationSec },
                    completedAtMillis = millis
                )
            }
    }

    // Calendar marks: all dates that appear in completedDateMap keys
    val completedDates = completedDateMap.keys.toSet()

    // Weekly report: current week (Mon-Sun) containing today
    val today = LocalDate.now()
    val startOfWeek = today.with(DayOfWeek.MONDAY)
    val endOfWeek = today.with(DayOfWeek.SUNDAY)

    val weekEntries = historyEntries.filter {
        !it.completedDate.isBefore(startOfWeek) && !it.completedDate.isAfter(endOfWeek)
    }
    val weekTotalSeconds = weekEntries.sumOf { it.durationSeconds }
    val weekTotalKcal = weekEntries.sumOf { entry ->
        entry.dayPlan.workoutExercises.sumOf { it.kcal }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // ── Calendar ─────────────────────────────────────────────────────────
        item {
            CalendarSection(
                displayedMonth = displayedMonth,
                completedDates = completedDates,
                today = today,
                onPrevMonth = { displayedMonth = displayedMonth.minusMonths(1) },
                onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) }
            )
        }

        item { Spacer(Modifier.height(28.dp)) }

        // ── Weekly Report header ──────────────────────────────────────────────
        item {
            Text(
                "Weekly Report",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        // ── Week summary card ─────────────────────────────────────────────────
        item {
            WeekSummaryCard(
                startOfWeek = startOfWeek,
                endOfWeek = endOfWeek,
                workoutCount = weekEntries.size,
                totalSeconds = weekTotalSeconds,
                totalKcal = weekTotalKcal
            )
            Spacer(Modifier.height(2.dp))
        }

        // ── Workout entries ───────────────────────────────────────────────────
        if (weekEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No workouts this week yet.\nKeep going! 💪",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(weekEntries) { entry ->
                WorkoutHistoryItem(entry = entry)
            }
        }
    }
}

// ─── Calendar ────────────────────────────────────────────────────────────────
@Composable
private fun CalendarSection(
    displayedMonth: YearMonth,
    completedDates: Set<LocalDate>,
    today: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val onBg = MaterialTheme.colorScheme.onBackground
    val surface = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Month navigator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = onBg)
            }
            Text(
                "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${displayedMonth.year}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = onBg,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = onBg)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Day-of-week headers (Sun → Sat)
        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            daysOfWeek.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onBg.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Build calendar grid
        val firstDay = displayedMonth.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7  // Sun=0, Mon=1 … Sat=6
        val daysInMonth = displayedMonth.lengthOfMonth()
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - firstDayOfWeek + 1
                    val date = if (dayNum in 1..daysInMonth) displayedMonth.atDay(dayNum) else null

                    val isToday = date == today
                    val isCompleted = date != null && date in completedDates

                    val bgColor by animateColorAsState(
                        targetValue = when {
                            isCompleted -> primary
                            isToday -> onBg
                            else -> Color.Transparent
                        },
                        animationSpec = tween(200),
                        label = "dayBg"
                    )
                    val textColor = when {
                        isCompleted || isToday -> MaterialTheme.colorScheme.background
                        else -> onBg.copy(alpha = if (date != null) 0.8f else 0f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(bgColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$dayNum",
                                        fontSize = 14.sp,
                                        fontWeight = if (isToday || isCompleted) FontWeight.Bold else FontWeight.Normal,
                                        color = textColor
                                    )
                                }
                            }
                            // Dot for today (not completed)
                            if (isToday && !isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .size(4.dp)
                                        .background(primary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Week summary card ────────────────────────────────────────────────────────
@Composable
private fun WeekSummaryCard(
    startOfWeek: LocalDate,
    endOfWeek: LocalDate,
    workoutCount: Int,
    totalSeconds: Int,
    totalKcal: Int
) {
    val fmt = java.time.format.DateTimeFormatter.ofPattern("MMM d")
    val startLabel = startOfWeek.format(fmt)
    val endLabel = endOfWeek.format(fmt)

    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeLabel = if (minutes > 0) "${minutes}m ${seconds}s" else "${totalSeconds}s"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "$startLabel – $endLabel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "$workoutCount workouts",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Text(
                        timeLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🔥", fontSize = 12.sp)
                    Text(
                        "${String.format("%.1f", totalKcal.toFloat() / 10)} Kcal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// ─── Single workout history row ───────────────────────────────────────────────
@Composable
private fun WorkoutHistoryItem(entry: WorkoutHistoryEntry) {
    val plan = entry.dayPlan
    val isRest = plan.isRest
    val kcal = plan.workoutExercises.sumOf { it.kcal }
    val durationSec = entry.durationSeconds
    val minutes = durationSec / 60
    val timeLabel = if (minutes > 0) "${minutes}m" else "${durationSec}s"

    // Build timestamp label: "Jun 4, 11:08 PM" or just "Jun 4" as fallback
    val dateTimeLabel = remember(entry.completedAtMillis, entry.completedDate) {
        if (entry.completedAtMillis > 0L) {
            val localDateTime = java.time.Instant
                .ofEpochMilli(entry.completedAtMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
            val fmt = java.time.format.DateTimeFormatter.ofPattern("MMM d, hh:mm a")
            localDateTime.format(fmt)
        } else {
            entry.completedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isRest)
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isRest) {
                    Text("☕", fontSize = 24.sp)
                } else {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Title + meta
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plan.title.ifBlank { "Day ${plan.dayNumber}" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                if (isRest) {
                    Text(
                        "Rest Day",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Duration
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Text(
                                timeLabel,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        // Kcal
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("🔥", fontSize = 10.sp)
                            Text(
                                "${String.format("%.1f", kcal.toFloat() / 10)} Kcal",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Timestamp label aligned to end (right side)
            Text(
                dateTimeLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                maxLines = 2
            )
        }
    }
}


// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun WorkoutHistoryScreenPreview() {
    FitflowTheme {
        WorkoutHistoryScreen()
    }
}
