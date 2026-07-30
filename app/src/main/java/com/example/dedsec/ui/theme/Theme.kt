package com.example.dedsec.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DedSecColorScheme = darkColorScheme(
    background = DedSecBackground,
    surface = DedSecSurface,
    primary = DedSecGreen,
    onPrimary = DedSecBackground,
    secondary = DedSecRed,
    onBackground = DedSecGreen,
    onSurface = DedSecGreen,
)

@Composable
fun DedSecTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DedSecColorScheme,
        typography = DedSecTypography,
        content = content
    )
}