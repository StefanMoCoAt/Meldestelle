package at.mocode.frontend.core.network

import kotlin.native.concurrent.ThreadLocal

/**
 * Network configuration with sensible defaults and environment overrides.
 * Defaults to the local API Gateway on port 8081.
 */
@ThreadLocal
object NetworkConfig {
    /**
     * Base URL for the API Gateway.
     * JVM: reads from ENV `API_BASE_URL`, falling back to http://localhost:8081
     * JS/WASM: uses compile-time or runtime override if provided, otherwise http://localhost:8081
     */
    val baseUrl: String = PlatformConfig.resolveApiBaseUrl()
}
