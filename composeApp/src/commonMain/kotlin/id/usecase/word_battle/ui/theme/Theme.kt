package id.usecase.word_battle.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E5BC9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A42),
    secondary = Color(0xFFAC29B6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF7D8F8),
    onSecondaryContainer = Color(0xFF37003B),
    tertiary = Color(0xFFFF7043),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBD0),
    onTertiaryContainer = Color(0xFF381A00),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF92B8FF),
    onPrimary = Color(0xFF002E6A),
    primaryContainer = Color(0xFF0E429A),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFFF0B0F5),
    onSecondary = Color(0xFF56005F),
    secondaryContainer = Color(0xFF7F148A),
    onSecondaryContainer = Color(0xFFFBD7FC),
    tertiary = Color(0xFFFFB59E),
    onTertiary = Color(0xFF5C1900),
    tertiaryContainer = Color(0xFFBF431D),
    onTertiaryContainer = Color(0xFFFFDBD0),
    background = Color(0xFF1A1C2B),
    onBackground = Color(0xFFE4E1E6),
    surface = Color(0xFF131421),
    onSurface = Color(0xFFE4E1E6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun WordBattleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}