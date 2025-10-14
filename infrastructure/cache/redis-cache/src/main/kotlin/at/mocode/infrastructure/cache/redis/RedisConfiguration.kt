package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.CacheConfiguration
import at.mocode.infrastructure.cache.api.CacheSerializer
import at.mocode.infrastructure.cache.api.DefaultCacheConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Redis connection properties.
 */
@ConfigurationProperties(prefix = "redis")
data class RedisProperties(
    val host: String = "localhost",
    val port: Int = 6379,
    val password: String? = null,
    val database: Int = 0,
    val connectionTimeout: Long = 2000,
    val readTimeout: Long = 2000,
    val usePooling: Boolean = true,
    val maxPoolSize: Int = 8,
    val minPoolSize: Int = 2
)

/**
 * Spring configuration for Redis.
 */
@Configuration
@EnableConfigurationProperties(RedisProperties::class)
class RedisConfiguration {

    /**
     * Creates a Redis connection factory.
     *
     * @param properties Redis connection properties
     * @return Redis connection factory
     */
    @Bean
    fun redisConnectionFactory(properties: RedisProperties): RedisConnectionFactory {
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
     * Creates a Redis template for byte arrays.
     *
     * @param connectionFactory Redis connection factory
     * @return Redis template
     */
    @Bean
    fun redisTemplate(
        @Qualifier("redisConnectionFactory") connectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, ByteArray> {
        return RedisTemplate<String, ByteArray>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            // Use default serializer for values (byte arrays)
            afterPropertiesSet()
        }
    }

    /**
     * Creates a cache serializer.
     *
     * @return Cache serializer
     */
    @Bean
    @ConditionalOnMissingBean
    fun cacheSerializer(): CacheSerializer {
        return JacksonCacheSerializer()
    }

    /**
     * Creates a default cache configuration if none is provided.
     *
     * @return Cache configuration
     */
    @Bean
    @ConditionalOnMissingBean
    fun cacheConfiguration(): CacheConfiguration {
        return DefaultCacheConfiguration()
    }
}
