package at.mocode.members.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Main application class for the Members Service.
 *
 * This service provides APIs for managing members and their data.
 */
@SpringBootApplication
@ComponentScan(basePackages = ["at.mocode.members"])
class MembersServiceApplication

/**
 * Main entry point for the Members Service application.
 */
fun main(args: Array<String>) {
    runApplication<MembersServiceApplication>(*args)
}
