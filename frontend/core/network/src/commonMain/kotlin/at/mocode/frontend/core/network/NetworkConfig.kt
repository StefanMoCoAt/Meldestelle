package at.mocode.frontend.core.network

import kotlin.native.concurrent.ThreadLocal

/**
 * Netzwerkkonfiguration mit sinnvollen Standardeinstellungen und Umgebungseinstellungen zum Überschreiben.
 * Standardmäßig wird das lokale API-Gateway auf Port 8081 verwendet.
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
