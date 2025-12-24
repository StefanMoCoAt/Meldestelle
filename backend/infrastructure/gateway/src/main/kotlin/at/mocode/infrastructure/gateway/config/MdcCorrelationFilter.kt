package at.mocode.infrastructure.gateway.config

import at.mocode.infrastructure.gateway.config.CorrelationIdFilter.Companion.CORRELATION_ID_HEADER
import org.slf4j.MDC
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Minimaler MDC-Filter: schreibt die vorhandene X-Correlation-ID in den MDC,
 * damit Logs die ID automatisch mitführen. Keine Body-/PII-Logs, nur Header-ID.
 *
 * Reihenfolge: direkt nach dem CorrelationIdFilter ausführen, damit die ID
 * bereits gesetzt ist. Daher Order = HIGHEST_PRECEDENCE + 1.
 */
@Component
class MdcCorrelationFilter : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val correlationId = exchange.request.headers.getFirst(CORRELATION_ID_HEADER)
        if (correlationId != null) {
            MDC.put(CORRELATION_ID_HEADER, correlationId)
        }

        return chain.filter(exchange)
            // Bei Abschluss säubern, um Leaks über Thread-Grenzen zu vermeiden
            .doFinally { MDC.remove(CORRELATION_ID_HEADER) }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1
}
