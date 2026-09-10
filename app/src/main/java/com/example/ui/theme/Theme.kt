package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF38BDF8), // Sky 400
        onPrimary = Color(0xFF0C4A6E),
        primaryContainer = Color(0xFF075985),
        onPrimaryContainer = Color(0xFFE0F2FE),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF0F172A),
        secondaryContainer = Color(0xFF1E293B),
        onSecondaryContainer = Color(0xFFBAE6FD),
        background = Color(0xFF0F172A), // Slate 900
        onBackground = Color(0xFFF8FAFC), // Slate 50
        surface = Color(0xFF1E293B), // Slate 800
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = Color(0xFF334155),
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = Color(0xFF475569),
        outlineVariant = Color(0xFF334155),
        error = Color(0xFFFB7185),
        onError = Color(0xFF4C0519),
        errorContainer = Color(0xFF881337),
        onErrorContainer = Color(0xFFFFE4E6)
    )

private val LightColorScheme =
    lightColorScheme(
        primary = FinancePrimary,
        onPrimary = FinanceOnPrimary,
        primaryContainer = FinancePrimaryContainer,
        onPrimaryContainer = FinanceOnPrimaryContainer,
        secondary = FinanceSecondary,
        onSecondary = FinanceOnSecondary,
        secondaryContainer = FinanceSecondaryContainer,
        onSecondaryContainer = FinanceOnSecondaryContainer,
        tertiary = FinanceTertiary,
        onTertiary = FinanceOnPrimary,
        tertiaryContainer = FinanceTertiaryContainer,
        onTertiaryContainer = FinanceOnTertiaryContainer,
        background = FinanceBackground,
        onBackground = FinanceOnBackground,
        surface = FinanceSurface,
        onSurface = FinanceOnSurface,
        surfaceVariant = FinanceSurfaceVariant,
        onSurfaceVariant = FinanceOnSurfaceVariant,
        outline = FinanceOutline,
        outlineVariant = FinanceOutlineVariant,
        error = FinanceError,
        onError = FinanceOnError,
        errorContainer = FinanceErrorContainer,
        onErrorContainer = FinanceOnErrorContainer
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

