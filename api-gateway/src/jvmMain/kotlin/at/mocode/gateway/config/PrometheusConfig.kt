package at.mocode.gateway.config

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

/**
 * Prometheus metrics configuration for the API Gateway.
 *
 * Configures Micrometer with Prometheus registry and exposes a metrics endpoint.
 */

// Create a Prometheus registry
val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

/**
 * Configures Prometheus metrics for the application.
 */
fun Application.configurePrometheusMetrics() {
    // Install Micrometer metrics
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        // JVM metrics
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            JvmThreadMetrics(),
            ClassLoaderMetrics(),
            ProcessorMetrics()
        )
    }

    // Add a route to expose Prometheus metrics with basic authentication
    routing {
        // Secure metrics endpoint with basic authentication
        authenticate("metrics-auth") {
            get("/metrics") {
                call.respond(appMicrometerRegistry.scrape())
            }
        }
    }

    log.info("Prometheus metrics configured and secured at /metrics endpoint")
}
