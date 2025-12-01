package at.mocode.events

/**
 * Simple Event Management class for testing KMP configuration
 */
class EventManagement {
    fun createEvent(name: String): String {
        return "Event created: $name"
    }
}

fun main() {
    val eventManager = EventManagement()
    println(eventManager.createEvent("Test Event"))
}
