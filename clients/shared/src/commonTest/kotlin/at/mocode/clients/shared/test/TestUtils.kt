package at.mocode.clients.shared.test

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

expect fun runBlockingTest(block: suspend () -> Unit)

abstract class BaseTest {
    @BeforeTest
    fun setupTest() {
        // Set up a common test environment
    }

    @AfterTest
    fun teardownTest() {
        // Cleanup test environment
    }
}
