package at.mocode.infrastructure.monitoring

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

/**
 * Testet, ob der Spring Application Context für den Monitoring-Server
 * erfolgreich geladen werden kann.
 *
 * Mit der Armeria Auto-Configuration Ausschluss-Konfiguration sollte der Context erfolgreich laden.
 * Verwendet RANDOM_PORT um Konflikte mit bootRun-Tasks zu vermeiden.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MonitoringServerApplicationTest {

    @Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun `context loads successfully`() {
        // Bestätigt, dass der gesamte Server-Kontext erfolgreich gestartet wurde.
        assertThat(context).isNotNull()
    }
}
