package at.mocode.frontend.core.network

import kotlinx.browser.window

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object PlatformConfig {
  actual fun resolveApiBaseUrl(): String {
    // 1) Prefer a global JS variable (can be injected by index.html or nginx)
    val fromGlobal = getGlobalApiBaseUrl()
    if (fromGlobal.isNotEmpty()) return fromGlobal.removeSuffix("/")

    // 2) Try window location origin (same origin gateway/proxy setup)
    // In Wasm, we can access a window directly if we are in the browser main thread.
    // However, we need to be careful about exceptions.
    val origin = try {
        window.location.origin
    } catch (e: Throwable) {
        null
    }

    if (!origin.isNullOrBlank()) return origin.removeSuffix("/")

    // 3) Fallback to the local gateway
    return "http://localhost:8081"
  }
}

// Helper function for JS interop in Wasm
// Kotlin/Wasm does not support 'dynamic' type or complex js() blocks inside functions.
// We must use top-level external functions or simple js() expressions.
private fun getGlobalApiBaseUrl(): String = js("""
    (function() {
        var global = typeof globalThis !== 'undefined' ? globalThis : (typeof window !== 'undefined' ? window : (typeof self !== 'undefined' ? self : {}));
        return (global.API_BASE_URL && typeof global.API_BASE_URL === 'string') ? global.API_BASE_URL : "";
    })()
""")
