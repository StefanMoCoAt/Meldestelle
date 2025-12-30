package at.mocode.infrastructure.gateway.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class RateLimitingConfig {

  /**
   * Einfache IP-basierte KeyResolver-Implementierung für das RequestRateLimiter-Filter.
   * Nutzt X-Forwarded-For, wenn vorhanden, sonst die Remote-Adresse.
   */
  @Bean
  @Primary
  fun ipAddressKeyResolver(): KeyResolver = KeyResolver { exchange ->
    val forwardedFor = exchange.request.headers.getFirst("X-Forwarded-For")
      ?.split(',')?.firstOrNull()?.trim()
    val ip = forwardedFor
      ?: exchange.request.remoteAddress?.address?.hostAddress
      ?: "unknown"
    Mono.just(ip)
  }
}
