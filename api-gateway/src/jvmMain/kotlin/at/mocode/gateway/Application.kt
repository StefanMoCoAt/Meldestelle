package at.mocode.gateway

import at.mocode.gateway.config.MigrationSetup
import at.mocode.shared.database.DatabaseConfig
import at.mocode.shared.database.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun main() {
    // Datenbank initialisieren
    val databaseConfig = DatabaseConfig.fromEnv()
    DatabaseFactory.init(databaseConfig)

    // Migrationen ausführen
    MigrationSetup.runMigrations()

    // Server starten
    embeddedServer(Netty, port = System.getenv("API_PORT")?.toIntOrNull() ?: 8081) {
        configureApplication()
    }.start(wait = true)
}

fun Application.configureApplication() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "OK"))
        }
    }
}
