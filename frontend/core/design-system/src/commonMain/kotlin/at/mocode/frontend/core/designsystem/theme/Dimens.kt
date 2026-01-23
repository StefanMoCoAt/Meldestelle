package at.mocode.frontend.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Zentrale Definition für Abstände und Größen.
 * Warum? Damit wir nicht überall "Magic Numbers" (z.B. 13.dp) haben.
 * Wenn wir den Abstand global ändern wollen, machen wir das nur hier.
 */
object Dimens {
    // Spacing (Abstände)
    val SpacingXS = 4.dp   // Sehr eng (für Tabellen, dichte Listen)
    val SpacingS = 8.dp    // Standard Abstand zwischen Elementen
    val SpacingM = 16.dp   // Abstand für Sektionen
    val SpacingL = 24.dp   // Außenabstand für Screens

    // Sizes (Größen)
    val IconSizeS = 16.dp
    val IconSizeM = 24.dp

    // Borders
    val BorderThin = 1.dp

    // Corner Radius (Ecken)
    val CornerRadiusS = 4.dp // Leicht abgerundet (Enterprise Look)
    val CornerRadiusM = 8.dp
}
