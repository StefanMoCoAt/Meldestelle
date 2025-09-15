package at.mocode.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mocode.Greeting

/**
 * Lazy-loadable Platform Info Component
 * This component is only loaded when needed to reduce initial bundle size
 */
@Composable
fun PlatformInfoComponent(
    showContent: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Platform Info Toggle Button
        Button(
            onClick = onToggle,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(if (showContent) "Platform-Info ausblenden" else "Platform-Info anzeigen")
        }

        // Lazy-loaded content - only create Greeting when actually shown
        AnimatedVisibility(showContent) {
            LazyPlatformInfo()
        }
    }
}

/**
 * Internal composable that's only loaded when AnimatedVisibility is active
 */
@Composable
private fun LazyPlatformInfo() {
    // This is only instantiated when showContent is true
    val greeting = remember { Greeting().greet() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = "Willkommen in der Meldestelle-Anwendung!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
