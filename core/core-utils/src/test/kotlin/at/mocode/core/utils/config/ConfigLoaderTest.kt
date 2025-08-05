package at.mocode.core.utils.config

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigLoaderTest {

    // JUnit 5 erstellt automatisch ein temporäres Verzeichnis für diesen Test
    @TempDir
    lateinit var tempDir: File

    private lateinit var configDir: File

    @BeforeEach
    fun setup() {
        // Wir erstellen unsere eigene 'config'-Verzeichnisstruktur im temporären Ordner
        configDir = File(tempDir, "config")
        configDir.mkdir()
    }

    @Test
    fun `load should use default values when no properties file is present`() {
        // Arrange
        // HINWEIS: Der Loader braucht den Pfad zum übergeordneten Temp-Verzeichnis.
        val configLoader = ConfigLoader(tempDir.absolutePath)

        // Act
        val config = configLoader.load(AppEnvironment.DEVELOPMENT)

        // Assert
        assertEquals("Meldestelle", config.appInfo.name)
        assertEquals(8081, config.server.port) // Standard-Port
    }

    @Test
    fun `load should read values from base application_properties`() {
        // Arrange
        // Erstelle eine Test-Konfigurationsdatei
        File(tempDir, "application.properties").writeText(
            """
            app.name=TestApp
            server.port=9999
        """.trimIndent()
        )

        // HINWEIS: Der Loader braucht den Pfad zum übergeordneten Temp-Verzeichnis.
        val configLoader = ConfigLoader(tempDir.absolutePath)

        // Act
        val config = configLoader.load(AppEnvironment.DEVELOPMENT)

        // Assert
        assertEquals("TestApp", config.appInfo.name)
        assertEquals(9999, config.server.port)
    }

    @Test
    fun `load should override base properties with environment-specific properties`() {
        // Arrange
        File(tempDir, "application.properties").writeText(
            """
            app.name=BaseApp
            server.port=8000
            database.host=base-db-host
            """.trimIndent()
        )

        File(tempDir, "application-test.properties").writeText(
            """
            app.name=TestEnvApp
            server.port=9000
            """.trimIndent()
        )

        // HINWEIS: Der Loader braucht den Pfad zum übergeordneten Temp-Verzeichnis.
        val configLoader = ConfigLoader(tempDir.absolutePath)

        // Act
        val config = configLoader.load(AppEnvironment.TEST)

        // Assert
        assertEquals(AppEnvironment.TEST, config.environment, "Environment should be TEST")
        assertEquals("TestEnvApp", config.appInfo.name, "app.name should be overridden")
        assertEquals(9000, config.server.port, "server.port should be overridden")
        assertEquals("base-db-host", config.database.host, "database.host should come from the base file")
    }
}
