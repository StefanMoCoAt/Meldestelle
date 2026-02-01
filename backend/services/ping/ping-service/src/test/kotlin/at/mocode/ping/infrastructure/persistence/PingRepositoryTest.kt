package at.mocode.ping.infrastructure.persistence

import at.mocode.ping.domain.Ping
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * Minimaler Integrationstest für die Persistenz-Schicht.
 * Prüft:
 * 1. Startet echte Postgres DB (via Testcontainers).
 * 2. Führt Flyway-Migrationen aus (Schema-Erstellung).
 * 3. Speichert und lädt eine Entity (Mapping-Check).
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [TestPersistenceConfig::class]) // Nutzt isolierte Config statt Main-App
@Import(PingRepositoryAdapter::class) // Importiert den Adapter, den wir testen wollen
@ActiveProfiles("test") // WICHTIG: Lädt application-test.yaml
@OptIn(ExperimentalUuidApi::class)
class PingRepositoryTest {

    @Autowired
    private lateinit var repositoryAdapter: PingRepositoryAdapter

    // Wir nutzen das Repository direkt, um zu prüfen, ob JPA funktioniert
    @Autowired
    private lateinit var springDataRepository: SpringDataPingRepository

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        @DynamicPropertySource
        fun registerPgProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            // Wichtig: Flyway muss laufen, um Tabellen zu erstellen
            registry.add("spring.flyway.enabled") { "true" }
        }
    }

    @Test
    fun `should save and load ping entity`() {
        // Given
        val pingId = Uuid.generateV7()
        val ping = Ping(
            id = pingId,
            message = "Integration Test Ping",
            timestamp = Instant.now()
        )

        // When
        repositoryAdapter.save(ping)

        // Then (via Adapter)
        val loadedPing = repositoryAdapter.findById(pingId)
        assertThat(loadedPing).isNotNull
        assertThat(loadedPing?.message).isEqualTo("Integration Test Ping")
        assertThat(loadedPing?.id).isEqualTo(pingId)

        // Then (Direct DB Check via Spring Data)
        // Fix: Use toJavaUuid() instead of UUID.fromString(pingId.toString())
        val entity = springDataRepository.findById(pingId.toJavaUuid())
        assertThat(entity).isPresent
        assertThat(entity.get().message).isEqualTo("Integration Test Ping")
    }
}
