package com.example.fitflow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Cấu hình bảng màu chuẩn theo Design System (Rise & Grind)
private val RiseAndGrindColorScheme = darkColorScheme(
    background = BaseBackground,     // #0D0D0D
    surface    = CardBackground,     // #1A1A1A
    surfaceVariant = CardNested,     // #222222
    primary    = OrangePrimary,      // #FF5500
    secondary  = CyanAccent,         // #00E5FF
    tertiary   = SuccessGreen,       // #22C55E
    onBackground = TextPrimary,      // #FFFFFF
    onSurface    = TextPrimary,      // #FFFFFF
    onSurfaceVariant = TextSecondary,// #888888
    onPrimary    = Color.White,      // Chữ trên nút màu cam luôn là màu trắng
    outline      = BorderDark        // #2A2A2A
)

@Composable
fun FitflowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = RiseAndGrindColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Đổi màu thanh trạng thái (Status Bar) tiệp với màu nền BaseBackground
            window.statusBarColor = colorScheme.background.toArgb()

            // Vì nền luôn tối, icon trên Status Bar (giờ, pin, wifi...) luôn phải là màu sáng (false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography, // Lấy từ file Type.kt
        shapes      = Shapes,     // Lấy từ file Shape.kt (bo góc 20dp, 16dp...)
        content     = content
    )
}
