package at.mocode.infrastructure.gateway.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import reactor.core.publisher.Mono
import java.security.Principal

@Configuration
class RateLimitConfig {

    /**
     * Standard KeyResolver: IP-basiert.
     * Nutzt X-Forwarded-For (für Proxies/LoadBalancer), wenn vorhanden, sonst die Remote-Adresse.
     * Wird verwendet, wenn kein anderer KeyResolver explizit angefordert wird oder aktiv ist.
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

    /**
     * Erweiterter KeyResolver: Principal-basiert (User-ID).
     * Versucht, den authentifizierten User (Principal) zu nutzen.
     * Fallback auf IP-Adresse, falls der User nicht eingeloggt ist.
     *
     * Aktivierung über Property: gateway.ratelimit.principal-key-resolver.enabled=true
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
