package at.mocode.clients.shared.test

import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
actual fun runBlockingTest(block: suspend () -> Unit) {
    GlobalScope.promise {
        block()
    }
}
