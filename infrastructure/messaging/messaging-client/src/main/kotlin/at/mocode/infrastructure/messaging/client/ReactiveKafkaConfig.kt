package at.mocode.infrastructure.messaging.client

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.kafka.sender.SenderOptions

@Configuration
class ReactiveKafkaConfig {

    @Bean
    fun reactiveKafkaProducerTemplate(producerFactory: ProducerFactory<String, Any>): ReactiveKafkaProducerTemplate<String, Any> {
        // Nutzt die ProducerFactory aus dem messaging-config-Modul
        val senderOptions = SenderOptions.create<String, Any>(producerFactory.configurationProperties)
        return ReactiveKafkaProducerTemplate(senderOptions)
    }
}
