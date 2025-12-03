package at.mocode.infrastructure.monitoring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Startet den Zipkin-Server.
 *
 * Spring Boot erkennt die 'zipkin-server'-Abhängigkeit im Classpath
 * und konfiguriert den Server automatisch. Eine explizite @EnableZipkinServer
 * Annotation ist nicht mehr erforderlich.
 */
@SpringBootApplication
class MonitoringServerApplication

fun main(args: Array<String>) {
    runApplication<MonitoringServerApplication>(*args)
}
