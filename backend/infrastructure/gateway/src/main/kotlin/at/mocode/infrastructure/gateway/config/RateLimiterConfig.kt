package at.mocode.infrastructure.gateway.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono
import java.security.Principal

@Configuration
class RateLimiterConfig {

  /**
   * KeyResolver basierend auf authentifiziertem Principal; Fallback auf Client-IP.
   * Funktioniert out-of-the-box mit Keycloak (Resource Server), sofern Security aktiv ist.
   */
  @Bean
  @ConditionalOnProperty(prefix = "gateway.ratelimit.principal-key-resolver", name = ["enabled"], havingValue = "true", matchIfMissing = false)
  fun principalNameKeyResolver(): KeyResolver = KeyResolver { exchange ->
    exchange.getPrincipal<Principal>()
      .map { it.name }
      .switchIfEmpty(
        Mono.just(
          exchange.request.headers.getFirst("X-Forwarded-For")?.split(",")?.first()?.trim()
            ?: exchange.request.headers.getFirst("X-Real-IP")
            ?: exchange.request.remoteAddress?.address?.hostAddress
            ?: "unknown"
        )
      )
  }
}
