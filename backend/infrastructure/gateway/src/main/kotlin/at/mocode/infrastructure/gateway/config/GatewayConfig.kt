package at.mocode.infrastructure.gateway.config

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

/**
 * Gateway-Konfiguration für erweiterte Funktionalitäten wie Logging, Rate Limiting und Security.
 */

/**
 * Global Filter für Correlations-IDs zur Request-Verfolgung.
 */
@Component
class CorrelationIdFilter : GlobalFilter, Ordered {

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-ID"
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val correlationId = request.headers.getFirst(CORRELATION_ID_HEADER)
            ?: UUID.randomUUID().toString()

        val mutatedRequest = request.mutate()
            .header(CORRELATION_ID_HEADER, correlationId)
            .build()

        val mutatedExchange = exchange.mutate()
            .request(mutatedRequest)
            .build()

        // Response-Header nach der Verarbeitung hinzufügen
        mutatedExchange.response.headers.add(CORRELATION_ID_HEADER, correlationId)

        return chain.filter(mutatedExchange)
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
