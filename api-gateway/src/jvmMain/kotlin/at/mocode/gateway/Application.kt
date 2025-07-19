package at.mocode.gateway

import at.mocode.gateway.config.MigrationSetup
import at.mocode.shared.config.AppConfig
import at.mocode.shared.database.DatabaseConfig
import at.mocode.shared.database.DatabaseFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    // Konfiguration laden (wird automatisch beim ersten Zugriff auf AppConfig initialisiert)
    val config = AppConfig

    // Datenbank initialisieren
    DatabaseFactory.init(config.database)

    // Migrationen ausführen
    MigrationSetup.runMigrations()

    // Server starten
    embeddedServer(Netty, port = config.server.port, host = config.server.host) {
        module()
    }.start(wait = true)
}

