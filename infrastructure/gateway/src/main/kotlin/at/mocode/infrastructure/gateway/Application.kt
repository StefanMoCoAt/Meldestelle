package at.mocode.infrastructure.gateway

import at.mocode.infrastructure.gateway.config.MigrationSetup
import at.mocode.core.utils.config.AppConfig
import at.mocode.core.utils.database.DatabaseFactory
import at.mocode.core.utils.discovery.ServiceRegistrationFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    // Konfiguration laden (wird automatisch beim ersten Zugriff auf AppConfig initialisiert)
    val config = AppConfig

    // Datenbank initialisieren
    DatabaseFactory.init(config.database)

    // Migrationen ausführen
    MigrationSetup.runMigrations()

    // Service mit Consul registrieren
    val serviceRegistration = if (config.serviceDiscovery.enabled && config.serviceDiscovery.registerServices) {
        ServiceRegistrationFactory.createServiceRegistration(
            serviceName = "api-gateway",
            servicePort = config.server.port,
            healthCheckPath = "/health",
            tags = listOf("api", "gateway"),
            meta = mapOf(
                "version" to config.appInfo.version,
                "environment" to config.environment.toString()
            )
        ).also { it.register() }
    } else null

    // Shutdown Hook hinzufügen, um Service bei Beendigung abzumelden
    Runtime.getRuntime().addShutdownHook(Thread {
        serviceRegistration?.deregister()
    })

    // Server starten
    embeddedServer(Netty, port = config.server.port, host = config.server.host) {
        module()
    }.start(wait = true)
}

