package at.mocode.infrastructure.monitoring

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

// Startet den ApplicationContext mit Webserver auf zufälligem Port und sicherer Testkonfiguration.
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "server.port=0",
        "management.server.port=0",
        "zipkin.storage.type=mem",
        "zipkin.self-tracing.enabled=false",
        "management.tracing.enabled=false",
        "management.zipkin.tracing.endpoint="
    ]
)
class MonitoringServerApplicationTest {

    @Test
    fun `context loads successfully`() {
        // Test ist bestanden, wenn der Kontext ohne Exception startet.
    }
}
