package at.mocode.infrastructure.messaging.client

import at.mocode.infrastructure.messaging.config.KafkaConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.kafka.sender.SenderOptions
import java.time.Duration

/**
 * Spring-Konfiguration für reaktive Kafka-Komponenten mit optimierten Einstellungen.
 */
@Configuration
class ReactiveKafkaConfig(
    private val kafkaConfig: KafkaConfig
) {

    private val logger = LoggerFactory.getLogger(ReactiveKafkaConfig::class.java)

    /**
     * Erstellt einen Spring-Bean für das optimierte ReactiveKafkaProducerTemplate.
     * Dieses Template beinhaltet erweiterte Fehlerbehandlung, Monitoring und Performance-Tuning.
     */
    @Bean
    fun reactiveKafkaProducerTemplate(): ReactiveKafkaProducerTemplate<String, Any> {
        logger.info("Creating optimized ReactiveKafkaProducerTemplate with enhanced configuration")

        val producerFactory = kafkaConfig.producerFactory()
        val props: Map<String, Any> = producerFactory.configurationProperties

        val senderOptions = SenderOptions.create<String, Any>(props)
            // Enhanced sender options for better performance and reliability
            .maxInFlight(1024) // Increase in-flight requests for better throughput
            .scheduler(reactor.core.scheduler.Schedulers.boundedElastic()) // Use bounded elastic scheduler
            .closeTimeout(Duration.ofSeconds(30)) // Give enough time for graceful shutdown
            .stopOnError(false) // Continue processing even if some messages fail

        return ReactiveKafkaProducerTemplate(senderOptions).apply {
            // Configure additional properties if needed
            logger.info("ReactiveKafkaProducerTemplate configured successfully with bootstrap servers: {}",
                kafkaConfig.bootstrapServers)
        }
    }

    /**
     * Erstellt einen KafkaConfig-Bean, falls nicht bereits vorhanden.
     * Ermöglicht externe Konfigurationsüberschreibung bei gleichzeitigen sinnvollen Defaults.
     */
    @Bean
    @ConditionalOnMissingBean(KafkaConfig::class)
    fun kafkaConfig(): KafkaConfig {
        return KafkaConfig().apply {
            logger.info("Initializing KafkaConfig with bootstrap servers: {}", bootstrapServers)
        }
    }
}
