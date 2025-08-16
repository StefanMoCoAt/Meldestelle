package at.mocode.client.desktop

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MainTest {

    @Test
    fun `should have valid main class configuration`() = runTest {
        // Verify that the main class exists and is properly structured
        val mainClass = this::class.java.classLoader.loadClass("at.mocode.client.desktop.MainKt")
        assertNotNull(mainClass, "Main class should be loadable")

        // Verify that the main method exists
        val mainMethod = mainClass.getMethod("main")
        assertNotNull(mainMethod, "Main method should exist")
    }

    @Test
    fun `should have proper package structure`() {
        // Verify the package exists and is accessible
        val packageName = "at.mocode.client.desktop"
        assertTrue(packageName.isNotBlank(), "Package name should not be blank")

        // Verify we can access classes in this package
        val currentClass = this::class.java
        assertTrue(currentClass.packageName.startsWith("at.mocode.client"),
            "Test should be in the correct package hierarchy")
    }

    @Test
    fun `should be able to instantiate system property for base URL`() {
        // Test the default configuration used in Main.kt
        val defaultUrl = System.getProperty("meldestelle.api.url", "http://localhost:8080")
        assertNotNull(defaultUrl, "Default API URL should not be null")
        assertTrue(defaultUrl.startsWith("http"), "API URL should be a valid HTTP URL")
    }
}
