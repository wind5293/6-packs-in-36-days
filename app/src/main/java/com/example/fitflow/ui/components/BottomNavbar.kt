package com.example.fitflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.House
import com.composables.icons.lucide.List
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.User

@Composable
fun BottomNavbar(currentRoute: String, onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            NavItem("Home", Lucide.House, currentRoute == "dashboard") {
                onNavigate("dashboard")
            }

            NavItem("Plan", Lucide.Calendar, currentRoute == "planner") {
                onNavigate("planner")
            }

            NavItem("Library", Lucide.List, currentRoute == "library") {
                onNavigate("library")
            }

            NavItem("Me", Lucide.User, currentRoute == "profile") {
                onNavigate("profile")
            }
        }
    }
}

@Composable
fun NavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
            alpha = 0.4f
        )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavbarPreview() {
    BottomNavbar("dashboard") {}
}
