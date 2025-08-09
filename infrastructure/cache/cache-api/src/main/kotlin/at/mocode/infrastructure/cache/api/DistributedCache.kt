package at.mocode.infrastructure.cache.api

import kotlin.time.Duration

interface DistributedCache {
    fun <T : Any> get(key: String, clazz: Class<T>): T?
    fun <T : Any> set(key: String, value: T, ttl: Duration? = null) // Geändert
    fun delete(key: String)
    fun exists(key: String): Boolean
    fun <T : Any> multiGet(keys: Collection<String>, clazz: Class<T>): Map<String, T>
    fun <T : Any> multiSet(entries: Map<String, T>, ttl: Duration? = null) // Geändert
    fun multiDelete(keys: Collection<String>)
    fun synchronize(keys: Collection<String>? = null)
    fun markDirty(key: String)
    fun getDirtyKeys(): Collection<String>
    fun clear()
}
