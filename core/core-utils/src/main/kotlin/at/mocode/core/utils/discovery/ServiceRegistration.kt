package at.mocode.core.utils.discovery

import at.mocode.core.utils.config.AppConfig // Angenommen, AppConfig ist jetzt eine Klasse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.util.*
import kotlin.time.Duration.Companion.seconds
import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration

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
            consul.agentClient().register(registration)
            isRegistered = true
            println("Service '${registration.name()}' mit ID '${registration.id()}' erfolgreich bei Consul registriert.")
        } catch (e: Exception) {
            println("FEHLER: Service-Registrierung bei Consul fehlgeschlagen: ${e.message}")
            // Optional: Fehler weiterwerfen, um den Anwendungsstart zu stoppen
        }
    }

    fun deregister() {
        if (!isRegistered) return
        try {
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

        val healthCheck = ImmutableRegistration.RegCheck.http(
            "http://$hostAddress:$servicePort/health", // Standard-Health-Check-Pfad
            10L // Intervall in Sekunden
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
            serviceRegistration.deregister()
        })

        return serviceRegistration
    }
}
