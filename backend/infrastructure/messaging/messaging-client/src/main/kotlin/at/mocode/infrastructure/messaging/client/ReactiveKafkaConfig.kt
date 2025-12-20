package at.mocode.infrastructure.messaging.client

import at.mocode.infrastructure.messaging.config.KafkaConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate

/**
 * Spring-Konfiguration für Kafka-Producer (KafkaTemplate-basiert).
 *
 * Hinweis: Der frühere ReactiveKafkaProducerTemplate ist nach dem Upgrade nicht mehr verfügbar.
 * Für echte Non-Blocking-Flows kann später selektiv auf Reactor Kafka migriert werden.
 */
@Configuration
class ReactiveKafkaConfig(
    private val kafkaConfig: KafkaConfig
) {

    private val logger = LoggerFactory.getLogger(ReactiveKafkaConfig::class.java)

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> {
        logger.info("Creating KafkaTemplate based on secure ProducerFactory configuration")
        val producerFactory = kafkaConfig.producerFactory()
        return KafkaTemplate(producerFactory).also {
            logger.info("KafkaTemplate configured successfully with bootstrap servers: {}", kafkaConfig.bootstrapServers)
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
