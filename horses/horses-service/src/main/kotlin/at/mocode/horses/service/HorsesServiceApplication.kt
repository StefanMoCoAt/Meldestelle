package at.mocode.horses.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for the Horses Service.
 *
 * This service provides APIs for managing horses and their data.
 */
@SpringBootApplication
class HorsesServiceApplication

/**
 * Main entry point for the Horses Service application.
 */
fun main(args: Array<String>) {
    runApplication<HorsesServiceApplication>(*args)
}
