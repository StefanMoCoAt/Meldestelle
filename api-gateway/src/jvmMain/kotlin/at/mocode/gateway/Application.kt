package at.mocode.gateway

import at.mocode.gateway.config.MigrationSetup
import at.mocode.shared.config.AppConfig
import at.mocode.shared.database.DatabaseFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*

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

