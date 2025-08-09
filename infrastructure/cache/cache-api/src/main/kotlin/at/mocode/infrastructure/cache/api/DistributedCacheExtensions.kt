package at.mocode.infrastructure.cache.api

/**
 * Kotlin-idiomatic extension function to retrieve a value from the cache
 * using reified types.
 *
 * Example: `val user = cache.get<User>("user:123")`
 */
inline fun <reified T : Any> DistributedCache.get(key: String): T? {
    return this.get(key, T::class.java)
}

/**
 * Kotlin-idiomatic extension function to retrieve multiple values from the cache
 * using reified types.
 *
 * Example: `val users = cache.multiGet<User>(listOf("user:123", "user:124"))`
 */
inline fun <reified T : Any> DistributedCache.multiGet(keys: Collection<String>): Map<String, T> {
    return this.multiGet(keys, T::class.java)
}
