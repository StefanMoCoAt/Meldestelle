package at.mocode

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HttpClientTest {

    @Test
    fun testServerConfiguration() {
        // Verify that the server port constant is set correctly
        assertEquals(8080, SERVER_PORT)
    }

    @Test
    fun testPlatformSpecificConfiguration() {
        // Verify that PlatformInfo.apiHost is accessible
        val apiHost = PlatformInfo.apiHost

        // The actual value depends on the platform (JVM vs WASM),
        // but we can at least verify it's not empty
        assertNotNull(apiHost)
        assertTrue(apiHost.isNotEmpty())

        // For WASM, it should be "localhost"
        // For JVM, it should be "backend"
        // But in a common test, we can only verify it's one of these values
        assertTrue(apiHost == "localhost" || apiHost == "backend",
            "Expected apiHost to be either 'localhost' or 'backend', but was '$apiHost'")
    }
}
