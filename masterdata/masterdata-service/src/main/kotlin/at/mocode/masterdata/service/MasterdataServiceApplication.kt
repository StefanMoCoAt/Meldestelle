package at.mocode.masterdata.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for the Masterdata Service.
 *
 * This service provides APIs for managing master data such as countries, regions, and other reference data.
 */
@SpringBootApplication
class MasterdataServiceApplication

/**
 * Main entry point for the Masterdata Service application.
 */
fun main(args: Array<String>) {
    runApplication<MasterdataServiceApplication>(*args)
}
