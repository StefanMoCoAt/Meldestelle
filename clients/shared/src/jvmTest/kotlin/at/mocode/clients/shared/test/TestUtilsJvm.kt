package at.mocode.clients.shared.test

import kotlinx.coroutines.test.*

actual fun runBlockingTest(block: suspend () -> Unit) {
    runTest {
        block()
    }
}
