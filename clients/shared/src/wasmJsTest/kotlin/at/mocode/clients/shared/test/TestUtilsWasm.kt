package at.mocode.clients.shared.test

import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class, ExperimentalWasmJsInterop::class)
actual fun runBlockingTest(block: suspend () -> Unit) {
    // WASM-JS uses the same approach as regular JS
    GlobalScope.promise {
        block()
    }
}
