package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.ChildTheme

private val ParentLightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = IndigoDark,
    secondary = Slate700,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate800,
    background = Color(0xFFF8FAFC),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Slate700,
    error = RoseDanger
)

private val ParentDarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Slate900,
    primaryContainer = IndigoDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Slate100,
    onSecondary = Slate900,
    secondaryContainer = Slate700,
    onSecondaryContainer = Slate100,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = RoseDanger
)

@Composable
fun KidLockTheme(
    isChildMode: Boolean = false,
    childTheme: ChildTheme = ChildTheme.SPACE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isChildMode) {
        darkColorScheme(
            primary = childTheme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = childTheme.secondaryColor,
            onPrimaryContainer = Color.White,
            secondary = childTheme.accentColor,
            onSecondary = Color.Black,
            background = childTheme.backgroundColor,
            onBackground = Color.White,
            surface = childTheme.surfaceColor,
            onSurface = Color.White,
            surfaceVariant = childTheme.surfaceColor.copy(alpha = 0.85f),
            onSurfaceVariant = Color(0xFFE2E8F0)
        )
    } else {
        if (darkTheme) ParentDarkColorScheme else ParentLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for template compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    KidLockTheme(
        isChildMode = false,
        darkTheme = darkTheme,
        content = content
    )
}
