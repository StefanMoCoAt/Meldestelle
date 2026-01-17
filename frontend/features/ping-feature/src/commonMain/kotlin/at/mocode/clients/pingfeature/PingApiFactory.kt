package at.mocode.clients.pingfeature

import at.mocode.ping.api.PingApi
import io.ktor.client.HttpClient

/**
 * Factory for providing a PingApi implementation.
 *
 * If an HttpClient is provided (e.g., DI-provided "apiClient"), a DI-aware
 * implementation is returned. Otherwise, a self-contained client is used
 * as a fallback to keep the feature working without DI.
 */
fun providePingApi(httpClient: HttpClient? = null): PingApi =
  if (httpClient != null) PingApiKoinClient(httpClient) else {
    // Fallback to a new KoinClient with a default HttpClient if none provided,
    // effectively removing the dependency on the deprecated PingApiClient
    // while maintaining the signature. Ideally, this path should not be hit in production.
    PingApiKoinClient(HttpClient())
  }
