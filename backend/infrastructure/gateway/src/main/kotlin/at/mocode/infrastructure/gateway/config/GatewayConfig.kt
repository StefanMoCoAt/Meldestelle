package at.mocode.infrastructure.gateway.config

import io.micrometer.tracing.Tracer
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Gateway-Konfiguration für erweiterte Funktionalitäten wie Logging, Rate Limiting und Security.
 */

/**
 * Globaler Filter, der sicherstellt, dass die Trace-ID (von Micrometer Tracing)
 * auch als "X-Correlation-ID" im Response-Header zurückgegeben wird.
 *
 * Hinweis: Micrometer Tracing kümmert sich bereits automatisch um die Propagation
 * der Trace-ID (b3 oder w3c) an nachgelagerte Services. Dieser Filter dient nur
 * der Bequemlichkeit für Clients (z. B. Frontend), um die ID einfach auslesen zu können.
 */
@Component
class CorrelationIdFilter(private val tracer: Tracer) : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(CorrelationIdFilter::class.java)

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-ID"
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        // Die aktuelle Trace-ID aus dem Micrometer Tracer holen
        val currentSpan = tracer.currentSpan()
        val traceId = currentSpan?.context()?.traceId()

        if (traceId != null) {
            // Trace-ID als Response-Header hinzufügen
            exchange.response.headers.add(CORRELATION_ID_HEADER, traceId)
        }

        return chain.filter(exchange)
            .doOnError { ex ->
                logger.error("Error processing request {}: {}", exchange.request.uri, ex.message)
            }
    }

    // Niedrige Priorität, damit Tracing-Kontext bereits initialisiert ist
    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE
}
