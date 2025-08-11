package at.mocode.infrastructure.messaging.client

import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.kafka.sender.SenderOptions

/**
 * Reactive Kafka configuration utilities for creating a ReactiveKafkaProducerTemplate.
 */
class ReactiveKafkaConfig {

    /**
     * Create a ReactiveKafkaProducerTemplate using the configuration from the given ProducerFactory.
     */
    fun reactiveKafkaProducerTemplate(
        producerFactory: DefaultKafkaProducerFactory<String, Any>
    ): ReactiveKafkaProducerTemplate<String, Any> {
        val props: Map<String, Any> = producerFactory.configurationProperties
        val senderOptions: SenderOptions<String, Any> = SenderOptions.create(props)
        return ReactiveKafkaProducerTemplate(senderOptions)
    }
}
