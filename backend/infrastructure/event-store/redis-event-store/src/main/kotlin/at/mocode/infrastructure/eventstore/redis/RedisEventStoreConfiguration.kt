package at.mocode.infrastructure.eventstore.redis

import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.EventStore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

/**
 * Redis Event Store Eigenschaften.
 */
@ConfigurationProperties(prefix = "redis.event-store")
data class RedisEventStoreProperties(
    var host: String = "localhost",
    var port: Int = 6379,
    var password: String? = null,
    var database: Int = 0,
    var connectionTimeout: Long = 2000,
    var readTimeout: Long = 2000,
    var usePooling: Boolean = true,
    var maxPoolSize: Int = 8,
    var minPoolSize: Int = 2,
    var consumerGroup: String = "event-processors",
    var consumerName: String = "event-consumer",
    var streamPrefix: String = "event-stream:",
    var allEventsStream: String = "all-events",
    var claimIdleTimeout: Duration = Duration.ofMinutes(1),
    var pollTimeout: Duration = Duration.ofMillis(100),
    var maxBatchSize: Int = 100,
    var createConsumerGroupIfNotExists: Boolean = true
)

/**
 * Spring-Konfiguration für Redis Event Store.
 */
@Configuration
@EnableConfigurationProperties(RedisEventStoreProperties::class)
class RedisEventStoreConfiguration {

    /**
     * Erstellt eine Redis-Verbindungsfactory für den Event Store.
     *
     * @param properties Redis Event Store Eigenschaften
     * @return Redis-Verbindungsfactory
     */
    @Bean
    @ConditionalOnMissingBean(name = ["eventStoreRedisConnectionFactory"])
    fun eventStoreRedisConnectionFactory(properties: RedisEventStoreProperties): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration().apply {
            hostName = properties.host
            port = properties.port
            properties.password?.let { password = RedisPassword.of(it) }
            database = properties.database
        }

        return LettuceConnectionFactory(config).apply {
            // Configure connection timeouts
            afterPropertiesSet()
        }
    }

    /**
     * Erstellt ein Redis-Template für den Event Store.
     *
     * @param connectionFactory Redis-Verbindungsfactory
     * @return Redis-Template
     */
    @Bean
    @ConditionalOnMissingBean(name = ["eventStoreRedisTemplate"])
    fun eventStoreRedisTemplate(
        @org.springframework.beans.factory.annotation.Qualifier("eventStoreRedisConnectionFactory")
        connectionFactory: RedisConnectionFactory
    ): StringRedisTemplate {
        return StringRedisTemplate().apply {
            setConnectionFactory(connectionFactory)
            afterPropertiesSet()
        }
    }

    /**
     * Erstellt einen Event-Serializer.
     *
     * @return Event-Serializer
     */
    @Bean
    @ConditionalOnMissingBean
    fun eventSerializer(): EventSerializer {
        return JacksonEventSerializer()
    }

    /**
     * Erstellt einen Redis Event Store.
     *
     * @param redisTemplate Redis-Template
     * @param eventSerializer Event-Serializer
     * @param properties Redis Event Store Eigenschaften
     * @return Event Store
     */
    @Bean
    @ConditionalOnMissingBean
    fun eventStore(
        @org.springframework.beans.factory.annotation.Qualifier("eventStoreRedisTemplate")
        redisTemplate: StringRedisTemplate,
        eventSerializer: EventSerializer,
        properties: RedisEventStoreProperties
    ): EventStore {
        return RedisEventStore(redisTemplate, eventSerializer, properties)
    }

    /**
     * Erstellt einen Redis Event Consumer.
     *
     * @param redisTemplate Redis-Template
     * @param eventSerializer Event-Serializer
     * @param properties Redis Event Store Eigenschaften
     * @return Event Consumer
     */
    @Bean
    @ConditionalOnMissingBean
    fun eventConsumer(
        @org.springframework.beans.factory.annotation.Qualifier("eventStoreRedisTemplate")
        redisTemplate: StringRedisTemplate,
        eventSerializer: EventSerializer,
        properties: RedisEventStoreProperties
    ): RedisEventConsumer {
        return RedisEventConsumer(redisTemplate, eventSerializer, properties)
    }
}
