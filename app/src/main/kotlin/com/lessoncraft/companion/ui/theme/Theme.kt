package com.lessoncraft.companion.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Light = lightColorScheme(primary = Color(0xFF4F46E5), onPrimary = Color.White, secondary = Color(0xFF0F766E), background = Color(0xFFF8FAFC), surface = Color.White)
private val Dark = darkColorScheme(primary = Color(0xFF818CF8), onPrimary = Color(0xFF0B1220), secondary = Color(0xFF5EEAD4), background = Color(0xFF0B1220), surface = Color(0xFF131C2E))

@Composable
fun CompanionTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> Dark
        else -> Light
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
