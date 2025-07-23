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
 * Redis event store properties.
 */
@ConfigurationProperties(prefix = "redis.event-store")
data class RedisEventStoreProperties(
    val host: String = "localhost",
    val port: Int = 6379,
    val password: String? = null,
    val database: Int = 0,
    val connectionTimeout: Long = 2000,
    val readTimeout: Long = 2000,
    val usePooling: Boolean = true,
    val maxPoolSize: Int = 8,
    val minPoolSize: Int = 2,
    val consumerGroup: String = "event-processors",
    val consumerName: String = "event-consumer",
    val streamPrefix: String = "event-stream:",
    val allEventsStream: String = "all-events",
    val claimIdleTimeout: Duration = Duration.ofMinutes(1),
    val pollTimeout: Duration = Duration.ofMillis(100),
    val maxBatchSize: Int = 100,
    val createConsumerGroupIfNotExists: Boolean = true
)

/**
 * Spring configuration for Redis event store.
 */
@Configuration
@EnableConfigurationProperties(RedisEventStoreProperties::class)
class RedisEventStoreConfiguration {

    /**
     * Creates a Redis connection factory for the event store.
     *
     * @param properties Redis event store properties
     * @return Redis connection factory
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
     * Creates a Redis template for the event store.
     *
     * @param connectionFactory Redis connection factory
     * @return Redis template
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
     * Creates an event serializer.
     *
     * @return Event serializer
     */
    @Bean
    @ConditionalOnMissingBean
    fun eventSerializer(): EventSerializer {
        return JacksonEventSerializer()
    }

    /**
     * Creates a Redis event store.
     *
     * @param redisTemplate Redis template
     * @param eventSerializer Event serializer
     * @param properties Redis event store properties
     * @return Event store
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
     * Creates a Redis event consumer.
     *
     * @param redisTemplate Redis template
     * @param eventSerializer Event serializer
     * @param properties Redis event store properties
     * @return Event consumer
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
