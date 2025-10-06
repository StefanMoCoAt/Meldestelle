package at.mocode.clients.shared.network

// WASM implementation using a simple counter-approach
// Since we don't have direct access to system time in WASM,
// we'll use a monotonic counter for relative timing
private var wasmTimeCounter: Long = 0L

actual fun currentTimeMillis(): Long {
    wasmTimeCounter += 1
    return wasmTimeCounter
}
