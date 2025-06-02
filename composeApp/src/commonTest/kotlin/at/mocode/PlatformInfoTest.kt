package at.mocode

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlatformInfoTest {

    @Test
    fun testPlatformInfoApiHost() {
        // Verify that the apiHost property is accessible
        val apiHost = PlatformInfo.apiHost

        // The actual value depends on the platform (JVM vs WASM),
        // but we can at least verify it's not empty
        assertNotNull(apiHost)
        assertTrue(apiHost.isNotEmpty())

        // Note: In a real test environment, we might want to use
        // platform-specific tests to verify the exact values:
        // - For JVM: assertEquals("backend", apiHost)
        // - For WASM: assertEquals("localhost", apiHost)
        // But in a common test, we can only verify it's not empty
    }

    @Test
    fun testServerPort() {
        // Verify that the SERVER_PORT constant has the expected value
        assertEquals(8081, SERVER_PORT)
    }
}
