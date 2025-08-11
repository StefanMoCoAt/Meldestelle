package at.mocode.infrastructure.monitoring.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class MonitoringClientAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MonitoringClientAutoConfiguration::class.java))

    @Test
    fun `should load monitoring properties correctly into the environment`() {
        // Arrange
        val expectedPropertyValue = "true"
        val propertyKey = "management.observations.http.server.requests.enabled"

        // Act & Assert
        contextRunner.run { context ->
            val actualPropertyValue = context.environment.getProperty(propertyKey)
            assertThat(actualPropertyValue).isEqualTo(expectedPropertyValue)
        }
    }
}
