package at.mocode.core.utils.discovery

import at.mocode.core.utils.config.AppConfig
import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
// KORREKTUR: Expliziter Import für die `Registration`-Klasse, die den `RegCheck` enthält.
import com.orbitz.consul.model.agent.Registration
import java.net.InetAddress
import java.util.*

/**
 * Repräsentiert die Registrierung eines einzelnen Service-Exemplars bei Consul.
 * Diese Klasse kümmert sich um den Lebenszyklus (Registrierung, Deregistrierung).
 */
class ServiceRegistration internal constructor(
    private val consul: Consul,
    private val registration: ImmutableRegistration
) {
    private var isRegistered = false

    fun register() {
        if (isRegistered) return
        try {
            // Der `register`-Aufruf ist korrekt, da das `registration`-Objekt
            // bereits außerhalb vollständig und korrekt gebaut wurde.
            consul.agentClient().register(registration)
            isRegistered = true
            println("Service '${registration.name()}' mit ID '${registration.id()}' erfolgreich bei Consul registriert.")
        } catch (e: Exception) {
            println("FEHLER: Service-Registrierung bei Consul fehlgeschlagen: ${e.message}")
            throw IllegalStateException("Could not register service with Consul", e)
        }
    }

    fun deregister() {
        if (!isRegistered) return
        try {
            // Der `deregister`-Aufruf ist korrekt. Er erwartet die Service-ID als einfachen String.
            consul.agentClient().deregister(registration.id())
            isRegistered = false
            println("Service '${registration.name()}' mit ID '${registration.id()}' erfolgreich bei Consul deregistriert.")
        } catch (e: Exception) {
            println("FEHLER: Service-Deregistrierung bei Consul fehlgeschlagen: ${e.message}")
        }
    }
}

/**
 * Zentraler Registrar, der beim Anwendungsstart Services registriert.
 * Diese Klasse wird einmalig mit der Gesamt-AppConfig initialisiert.
 */
class ServiceRegistrar(private val appConfig: AppConfig) {

    private val consul: Consul by lazy {
        val consulConfig = appConfig.serviceDiscovery
        Consul.builder()
            .withUrl("http://${consulConfig.consulHost}:${consulConfig.consulPort}")
            .build()
    }

    /**
     * Erstellt und registriert einen Service basierend auf der App-Konfiguration.
     * @return Eine ServiceRegistration-Instanz zur Verwaltung des Lebenszyklus.
     */
    fun registerCurrentService(): ServiceRegistration {
        val serviceName = appConfig.appInfo.name
        val servicePort = appConfig.server.port
        val serviceId = "$serviceName-${UUID.randomUUID()}"
        val hostAddress = InetAddress.getLocalHost().hostAddress

        // KORREKTUR: Der Health Check MUSS über die statische Factory-Methode `http`
        // der `Registration.RegCheck`-Klasse erstellt werden. Dies war die Hauptfehlerquelle.
        val healthCheck = Registration.RegCheck.http(
            "http://$hostAddress:$servicePort/health", // Standard-Health-Check-Pfad
            10L, // Intervall in Sekunden
            5L // Timeout in Sekunden
        )

        val registration = ImmutableRegistration.builder()
            .id(serviceId)
            .name(serviceName)
            .address(hostAddress)
            .port(servicePort)
            .check(healthCheck)
            .tags(listOf("env:${appConfig.environment.name.lowercase()}"))
            .meta(mapOf("version" to appConfig.appInfo.version))
            .build()

        val serviceRegistration = ServiceRegistration(consul, registration)
        serviceRegistration.register()

        // Fügt einen Shutdown-Hook hinzu, um den Service beim Beenden sauber zu deregistrieren
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Shutdown-Hook: Deregistriere Service ${serviceId}...")
            serviceRegistration.deregister()
        })

        return serviceRegistration
    }
}
