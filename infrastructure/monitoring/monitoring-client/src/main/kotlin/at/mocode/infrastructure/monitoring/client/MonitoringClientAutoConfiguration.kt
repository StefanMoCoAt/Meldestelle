package at.mocode.infrastructure.monitoring.client

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.PropertySource

/**
 * AutoConfiguration für das Monitoring-Client-Modul.
 *
 * Lädt konservative Default-Properties mit niedriger Priorität, die in jeder Anwendung
 * leicht per application.properties/-yaml überschrieben werden können.
 */
@AutoConfiguration
@ConditionalOnClass(name = [
    "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration",
    "io.micrometer.core.instrument.MeterRegistry"
])
@PropertySource("classpath:monitoring-defaults.properties")
class MonitoringClientAutoConfiguration
