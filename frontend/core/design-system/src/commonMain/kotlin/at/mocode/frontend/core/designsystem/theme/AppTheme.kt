package at.mocode.frontend.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- 1. Farben (Palette) ---
// Wir definieren eine professionelle, kontrastreiche Palette.
// Blau steht für Aktion/Information, Grau für Struktur.

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0052CC),       // Enterprise Blue (stark)
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEEBFF),
    onPrimaryContainer = Color(0xFF0052CC),

    secondary = Color(0xFF2684FF),     // Helleres Blau für Akzente
    onSecondary = Color.White,

    background = Color(0xFFF4F5F7),    // Helles Grau (nicht hartes Weiß)
    surface = Color.White,
    onBackground = Color(0xFF172B4D),  // Fast Schwarz (besser lesbar als #000)
    onSurface = Color(0xFF172B4D),

    error = Color(0xFFDE350B),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4C9AFF),       // Helleres Blau auf Dunkel
    onPrimary = Color(0xFF091E42),
    primaryContainer = Color(0xFF0052CC),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF2684FF),
    onSecondary = Color.White,

    background = Color(0xFF1E1E1E),    // Dunkles Grau (angenehmer als #000)
    surface = Color(0xFF2C2C2C),       // Panels heben sich leicht ab
    onBackground = Color(0xFFEBECF0),
    onSurface = Color(0xFFEBECF0),

    error = Color(0xFFFF5630),
    onError = Color.Black
)

// --- 2. Formen (Shapes) ---
// Enterprise Apps nutzen oft weniger Rundung als Consumer Apps (seriöser).
private val AppShapes = Shapes(
    small = RoundedCornerShape(Dimens.CornerRadiusS),  // Buttons, Inputs
    medium = RoundedCornerShape(Dimens.CornerRadiusM), // Cards, Dialogs
    large = RoundedCornerShape(Dimens.CornerRadiusM)
)

// --- 3. Typografie ---
// wir setzen auf klare Hierarchie.
private val AppTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle( // Standard Text
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle( // Für dichte Tabellen/Labels
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false, // Kann später via Settings gesteuert werden
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}
