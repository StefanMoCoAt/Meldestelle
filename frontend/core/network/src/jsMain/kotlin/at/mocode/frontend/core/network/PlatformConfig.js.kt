package at.mocode.frontend.core.network

import kotlinx.browser.window

@Suppress("UnsafeCastFromDynamic", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object PlatformConfig {
  actual fun resolveApiBaseUrl(): String {
    // 1) Prefer a global JS variable (can be injected by index.html or nginx)
    val global =
      js("typeof globalThis !== 'undefined' ? globalThis : (typeof window !== 'undefined' ? window : (typeof self !== 'undefined' ? self : {}))")
    val fromGlobal = try {
      (global.API_BASE_URL as? String)?.trim().orEmpty()
    } catch (_: dynamic) {
      ""
    }
    if (fromGlobal.isNotEmpty()) {
      console.log("[PlatformConfig] Resolved API_BASE_URL from global: $fromGlobal")
      return fromGlobal.removeSuffix("/")
    }

    // 2) Try window location origin (same origin gateway/proxy setup)
    val origin = try {
      window.location.origin
    } catch (_: dynamic) {
      null
    }

    if (!origin.isNullOrBlank()) {
      val resolvedUrl = origin.removeSuffix("/") + "/api"
      console.log("[PlatformConfig] Resolved API_BASE_URL from window.location.origin: $resolvedUrl")
      return resolvedUrl
    }

    // 3) Fallback to the local gateway directly (e.g. for tests without window)
    val fallbackUrl = "http://localhost:8081/api"
    console.log("[PlatformConfig] Fallback API_BASE_URL: $fallbackUrl")
    return fallbackUrl
  }
}
