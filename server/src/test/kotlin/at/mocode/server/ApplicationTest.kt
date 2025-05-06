package at.mocode.server

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Basic tests for the application
 */
class ApplicationTest {
    private val logger = LoggerFactory.getLogger(ApplicationTest::class.java)

    @Test
    fun testEnvironmentSetup() {
        // Set test environment flag
        System.setProperty("isTestEnvironment", "true")

        // Verify the flag is set correctly
        assertTrue(System.getProperty("isTestEnvironment").toBoolean())
        logger.info("Test environment flag set successfully")
    }

    @Test
    fun testApplicationFilesExist() {
        // Verify the Application.kt file exists
        val applicationFile = File("src/main/kotlin/at/mocode/server/Application.kt")
        assertTrue(applicationFile.exists() || File("server/" + applicationFile.path).exists(),
                   "Application.kt file should exist")

        // Verify the Database.kt file exists
        val databaseFile = File("src/main/kotlin/at/mocode/server/plugins/Database.kt")
        assertTrue(databaseFile.exists() || File("server/" + databaseFile.path).exists(),
                   "Database.kt file should exist")

        logger.info("Application files exist")
    }

    @Test
    fun testConfigurationFileExists() {
        // Verify the application.yaml file exists
        val configFile = File("src/main/resources/application.yaml")
        assertTrue(configFile.exists() || File("server/" + configFile.path).exists(),
                   "application.yaml file should exist")

        logger.info("Configuration file exists")
    }
}
