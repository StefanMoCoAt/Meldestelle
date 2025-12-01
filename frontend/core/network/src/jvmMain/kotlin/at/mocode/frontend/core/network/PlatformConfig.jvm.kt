package at.mocode.frontend.core.network

actual object PlatformConfig {
  actual fun resolveApiBaseUrl(): String {
    // Prefer environment variable
    val env = System.getenv("API_BASE_URL")?.trim().orEmpty()
    if (env.isNotEmpty()) return env.removeSuffix("/")
    // Fallback default to the local gateway
    return "http://localhost:8081"
  }
}
