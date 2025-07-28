package at.mocode.masterdata.service

import at.mocode.core.utils.config.AppConfig
import at.mocode.core.utils.database.DatabaseFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for the Masterdata Service.
 *
 * This service provides APIs for managing master data such as countries, regions, and other reference data.
 */
@SpringBootApplication
class MasterdataServiceApplication

fun main(args: Array<String>) {
    // 1. Lade die Konfiguration explizit, genau einmal beim Start.
    val appConfig = AppConfig.load()
    println("Konfiguration für Umgebung '${appConfig.environment}' geladen.")

    // 2. Initialisiere die Datenbank mit der geladenen Konfiguration.
    //    Flyway-Migrationen werden hier automatisch ausgeführt.
    DatabaseFactory.init(appConfig.database)
    println("Datenbank initialisiert und migriert.")

    // 3. Starte die Spring Boot / Ktor Anwendung.
    //    Der appConfig-Wert kann hier an die Anwendung übergeben werden,
    //    um ihn später per Dependency Injection zu nutzen.
    runApplication<MasterdataServiceApplication>(*args)
}
