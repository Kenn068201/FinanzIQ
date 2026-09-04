package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = FinanceSecondary,
        onPrimary = FinanceOnPrimary,
        primaryContainer = FinancePrimary,
        onPrimaryContainer = FinancePrimaryContainer,
        secondary = FinanceSecondary,
        onSecondary = FinanceOnSecondary,
        secondaryContainer = FinanceSecondaryContainer,
        onSecondaryContainer = FinanceOnSecondaryContainer,
        background = FinanceBackground,
        onBackground = FinanceOnBackground,
        surface = FinanceSurface,
        onSurface = FinanceOnSurface,
        surfaceVariant = FinanceSurfaceVariant,
        onSurfaceVariant = FinanceOnSurfaceVariant,
        error = FinanceError,
        onError = FinanceOnError,
        errorContainer = FinanceErrorContainer,
        onErrorContainer = FinanceOnErrorContainer
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

