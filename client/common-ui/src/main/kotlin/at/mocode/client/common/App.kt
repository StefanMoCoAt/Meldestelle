package at.mocode.client.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import at.mocode.client.common.theme.MeldestelleTheme

/**
 * Base application theme wrapper for consistent UI across all applications.
 * This is a simplified version that just applies the theme.
 * Specific applications should implement their own App composable with navigation.
 */
@Composable
fun BaseApp(content: @Composable () -> Unit) {
    MeldestelleTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
