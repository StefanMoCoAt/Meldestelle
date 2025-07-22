package at.mocode.members.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for the Members Service.
 *
 * This service provides APIs for managing members and their data.
 */
@SpringBootApplication
class MembersServiceApplication

/**
 * Main entry point for the Members Service application.
 */
fun main(args: Array<String>) {
    runApplication<MembersServiceApplication>(*args)
}
