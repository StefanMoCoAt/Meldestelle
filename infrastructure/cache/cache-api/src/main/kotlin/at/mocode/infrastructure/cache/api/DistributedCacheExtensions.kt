package at.mocode.infrastructure.cache.api

/**
 * Kotlin-idiomatische Extension-Funktion, um einen Wert aus dem Cache zu lesen
 * – mit reified Typen.
 *
 * Beispiel: `val user = cache.get<User>("user:123")`
 */
inline fun <reified T : Any> DistributedCache.get(key: String): T? {
    return this.get(key, T::class.java)
}

/**
 * Kotlin-idiomatische Extension-Funktion, um mehrere Werte aus dem Cache zu lesen
 * – mit reified Typen.
 *
 * Beispiel: `val users = cache.multiGet<User>(listOf("user:123", "user:124"))`
 */
inline fun <reified T : Any> DistributedCache.multiGet(keys: Collection<String>): Map<String, T> {
    return this.multiGet(keys, T::class.java)
}
