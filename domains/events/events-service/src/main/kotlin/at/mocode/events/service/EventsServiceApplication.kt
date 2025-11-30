package at.mocode.events.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for the Events Service.
 *
 * This service provides APIs for managing events and competitions.
 */
@SpringBootApplication
class EventsServiceApplication

/**
 * Main entry point for the Events Service application.
 */
fun main(args: Array<String>) {
    runApplication<EventsServiceApplication>(*args)
}
