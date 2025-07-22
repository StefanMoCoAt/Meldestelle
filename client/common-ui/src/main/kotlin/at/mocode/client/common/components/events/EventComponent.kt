package at.mocode.client.common.components.events

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mocode.events.domain.model.Veranstaltung

/**
 * Utility functions for event display in Compose UI
 * This is a Compose-based replacement for the JS-specific EventUIUtils
 */
object EventComposeUtils {

    /**
     * Formats an event as a summary string
     */
    fun formatEventSummary(event: Veranstaltung): String {
        return buildString {
            append("${event.name}")
            append(" | ${event.ort}")
            append(" | ${event.startDatum}")
            if (event.isMultiDay()) {
                append(" - ${event.endDatum}")
            }
        }
    }

    /**
     * Returns a formatted date range string for an event
     */
    fun formatEventDateRange(event: Veranstaltung): String {
        return if (event.isMultiDay()) {
            "${event.startDatum} - ${event.endDatum} (${event.getDurationInDays()} Tage)"
        } else {
            "${event.startDatum} (Eintägige Veranstaltung)"
        }
    }

    /**
     * Returns a list of status indicators for an event
     */
    fun getEventStatusList(event: Veranstaltung): List<String> {
        val statusList = mutableListOf<String>()
        if (event.istAktiv) statusList.add("Aktiv")
        if (event.istOeffentlich) statusList.add("Öffentlich")
        if (event.isRegistrationOpen()) statusList.add("Anmeldung offen")
        return statusList
    }
}

/**
 * A compact event card for displaying basic event information
 */
@Composable
fun CompactEventCard(
    event: Veranstaltung,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.ort,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = EventComposeUtils.formatEventDateRange(event),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Status indicators
            val statusList = EventComposeUtils.getEventStatusList(event)
            if (statusList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Status: ${statusList.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * A badge that displays the event status
 */
@Composable
fun EventStatusBadge(event: Veranstaltung) {
    val statusList = EventComposeUtils.getEventStatusList(event)
    if (statusList.isNotEmpty()) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = statusList.first(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
