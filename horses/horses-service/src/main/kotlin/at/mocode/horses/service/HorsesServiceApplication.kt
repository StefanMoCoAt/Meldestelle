package at.mocode.horses.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Main application class for the Horses Service.
 *
 * This service provides APIs for managing horses and their data.
 */
@SpringBootApplication
@ComponentScan(basePackages = [
    "at.mocode.horses.service",
    "at.mocode.horses.api",
    "at.mocode.horses.infrastructure",
    "at.mocode.infrastructure.messaging"
])
class HorsesServiceApplication

/**
 * Main entry point for the Horses Service application.
 */
fun main(args: Array<String>) {
    runApplication<HorsesServiceApplication>(*args)
}
