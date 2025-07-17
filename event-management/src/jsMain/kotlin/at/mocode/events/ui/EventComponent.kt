package at.mocode.events.ui

import at.mocode.events.domain.model.Veranstaltung

/**
 * Simple JS-specific utility functions for event management UI
 */
object EventUIUtils {

    /**
     * Formats an event for display in the browser
     */
    fun formatEventForDisplay(event: Veranstaltung): String {
        return buildString {
            append("Event: ${event.name}")
            append(" | Location: ${event.ort}")
            append(" | From: ${event.startDatum} to: ${event.endDatum}")
            if (event.beschreibung != null) {
                append(" | Description: ${event.beschreibung}")
            }
            if (event.sparten.isNotEmpty()) {
                append(" | Sports: ${event.sparten.joinToString(", ") { it.name }}")
            }
        }
    }

    /**
     * Creates a simple HTML representation of an event
     */
    fun createEventHtml(event: Veranstaltung): String {
        return """
            <div class="event-card">
                <h3>${event.name}</h3>
                <p><strong>Location:</strong> ${event.ort}</p>
                <p><strong>Date:</strong> ${event.startDatum} - ${event.endDatum}</p>
                ${if (event.beschreibung != null) "<p><strong>Description:</strong> ${event.beschreibung}</p>" else ""}
                ${if (event.sparten.isNotEmpty())
                    "<p><strong>Sports:</strong> ${event.sparten.joinToString(", ") { it.name }}</p>"
                  else ""}
            </div>
        """.trimIndent()
    }
}

/**
 * Main entry point for the JS application
 */
fun main() {
    console.log("Event Management JS module loaded successfully!")
    console.log("React dependencies are available for UI development")
}
