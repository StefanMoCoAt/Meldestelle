package at.mocode.frontend.core.network

import kotlinx.browser.window

@Suppress("UnsafeCastFromDynamic")
actual object PlatformConfig {
    actual fun resolveApiBaseUrl(): String {
        // 1) Prefer a global JS variable (can be injected by index.html or nginx)
        val global = js("typeof globalThis !== 'undefined' ? globalThis : (typeof window !== 'undefined' ? window : (typeof self !== 'undefined' ? self : {}))")
        val fromGlobal = try { (global.API_BASE_URL as? String)?.trim().orEmpty() } catch (_: dynamic) { "" }
        if (!fromGlobal.isNullOrEmpty()) return fromGlobal.removeSuffix("/")

        // 2) Try window location origin (same origin gateway/proxy setup)
        val origin = try { window.location.origin } catch (_: dynamic) { null }
        if (!origin.isNullOrBlank()) return origin.removeSuffix("/")

        // 3) Fallback to local gateway
        return "http://localhost:8081"
    }
}
