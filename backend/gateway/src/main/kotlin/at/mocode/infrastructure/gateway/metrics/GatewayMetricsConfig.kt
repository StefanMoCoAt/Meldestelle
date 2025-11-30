package at.mocode.infrastructure.gateway.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.config.MeterFilter
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Konfiguration für Gateway-spezifische Metriken mit Micrometer.
 *
 * Diese Konfiguration stellt folgende Metriken bereit:
 * - Request/Response Zeit Metriken (Timer)
 * - Fehlerrate Tracking (Counter)
 * - Custom Business Metrics
 *
 * Alle Metriken werden automatisch an Prometheus exportiert durch die
 * bestehende monitoring-client Integration.
 */
@Configuration
class GatewayMetricsConfig {

    companion object {
        // Metric Namen als Konstanten für bessere Wartbarkeit
        const val GATEWAY_REQUEST_TIMER = "gateway_custom_request_duration"
        const val GATEWAY_ERROR_COUNTER = "gateway_errors_total"
        const val GATEWAY_REQUESTS_COUNTER = "gateway_requests_total"
        const val GATEWAY_CIRCUIT_BREAKER_COUNTER = "gateway_circuit_breaker_events_total"
        const val GATEWAY_DOWNSTREAM_HEALTH_GAUGE = "gateway_downstream_health_status"
    }

    /**
     * Konfiguriert globale Meter-Registry Einstellungen für das Gateway.
     */
    @Bean
    fun gatewayMeterRegistryCustomizer(): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry ->
            // Gemeinsame Tags für alle Gateway-Metriken
            registry.config()
                .commonTags("service", "gateway", "component", "infrastructure")
                // Filterung von zu detaillierten Metriken
                .meterFilter(MeterFilter.deny { id ->
                    val name = id.name
                    // Ausschluss von internen Spring/Netty Metriken, die zu viel Rauschen erzeugen
                    name.startsWith("reactor.netty.connection.provider") ||
                    name.startsWith("reactor.netty.bytebuf.allocator") ||
                    name.startsWith("jvm.gc.overhead")
                })
                // Histogram-Buckets für Request Duration optimieren
                .meterFilter(MeterFilter.accept())
        }
    }

    /**
     * WebFilter für automatische Request/Response Zeit und Error Rate Tracking.
     *
     * Dieser Filter misst:
     * - Gesamte Request-Verarbeitungszeit
     * - Anzahl der Requests nach Status-Code kategorisiert
     * - Error-Rate basierend auf HTTP Status Codes
     */
    @Bean
    fun gatewayMetricsWebFilter(meterRegistry: MeterRegistry): WebFilter {
        return GatewayMetricsWebFilter(meterRegistry)
    }

    /**
     * Bean für Request Duration Timer - entfernt um Konflikte mit dem WebFilter zu vermeiden.
     * Die Request-Zeiten werden automatisch im GatewayMetricsWebFilter erfasst.
     */
    // @Bean - entfernt, um Prometheus Meter-Konflikte zu vermeiden,
    // fun requestTimer(meterRegistry: MeterRegistry): Timer { ... }

    /**
     * Bean für Error Counter - ermöglicht manuelles Error Tracking.
     */
    @Bean
    fun errorCounter(meterRegistry: MeterRegistry): Counter {
        return Counter.builder(GATEWAY_ERROR_COUNTER)
            .description("Gesamtanzahl der Gateway-Fehler")
            .register(meterRegistry)
    }

    /**
     * Bean für Request Counter - ermöglicht Request-Volumen Tracking.
     * Hinweis: Dieser Counter wird nur als Fallback registriert.
     * Die tatsächlichen Requests werden mit dynamischen Tags im WebFilter erfasst.
     */
    @Bean
    fun requestCounter(meterRegistry: MeterRegistry): Counter {
        return Counter.builder("${GATEWAY_REQUESTS_COUNTER}_fallback")
            .description("Gateway-Requests Fallback Counter")
            .register(meterRegistry)
    }

    /**
     * Bean für Circuit Breaker Events Counter.
     */
    @Bean
    fun circuitBreakerCounter(meterRegistry: MeterRegistry): Counter {
        return Counter.builder(GATEWAY_CIRCUIT_BREAKER_COUNTER)
            .description("Circuit Breaker Events im Gateway")
            .register(meterRegistry)
    }
}

/**
 * WebFilter Implementation für automatische Metrics-Erfassung.
 */
class GatewayMetricsWebFilter(private val meterRegistry: MeterRegistry) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = System.nanoTime()
        val request = exchange.request
        val path = request.path.value()
        val method = request.method.toString()

        // Request Counter incrementer
        Counter.builder(GatewayMetricsConfig.GATEWAY_REQUESTS_COUNTER)
            .tag("method", method)
            .tag("path", normalizePath(path))
            .description("Gateway-Requests gesamt")
            .register(meterRegistry)
            .increment()

        return chain.filter(exchange)
            .doFinally { _ ->
                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                val response = exchange.response
                val statusCode = response.statusCode?.value()?.toString() ?: "unknown"
                val statusSeries = when {
                    statusCode.startsWith("2") -> "2xx"
                    statusCode.startsWith("3") -> "3xx"
                    statusCode.startsWith("4") -> "4xx"
                    statusCode.startsWith("5") -> "5xx"
                    else -> "unknown"
                }

                // Request Duration Timer
                Timer.builder(GatewayMetricsConfig.GATEWAY_REQUEST_TIMER)
                    .tag("method", method)
                    .tag("path", normalizePath(path))
                    .tag("status", statusCode)
                    .tag("status_series", statusSeries)
                    .description("Gateway Request-Verarbeitungszeit")
                    .register(meterRegistry)
                    .record(duration)

                // Error Counter für 4xx und 5xx Responses
                if (statusCode.startsWith("4") || statusCode.startsWith("5")) {
                    Counter.builder(GatewayMetricsConfig.GATEWAY_ERROR_COUNTER)
                        .tag("method", method)
                        .tag("path", normalizePath(path))
                        .tag("status", statusCode)
                        .tag("status_series", statusSeries)
                        .tag("error_type", if (statusCode.startsWith("4")) "client_error" else "server_error")
                        .description("Gateway-Fehleranzahl")
                        .register(meterRegistry)
                        .increment()
                }
            }
    }

    /**
     * Normalisiert Pfade für Metriken, um Kardinalität-Explosion zu vermeiden.
     * Beispiel: /api/horses/123 → /api/horses/{id}
     */
    private fun normalizePath(path: String): String {
        return path
            // UUID pattern ersetzen
            .replace(Regex("/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"), "/{uuid}")
            // Numerische IDs ersetzen
            .replace(Regex("/\\d+"), "/{id}")
            // Sehr lange Pfade kürzen
            .let { if (it.length > 100) "${it.substring(0, 97)}..." else it }
    }
}
