package at.mocode.infrastructure.cache.api

/**
 * Interface for serializing and deserializing cache entries.
 */
interface CacheSerializer {
    /**
     * Serializes a value to a byte array.
     *
     * @param value The value to serialize
     * @return The serialized value as a byte array
     */
    fun <T : Any> serialize(value: T): ByteArray

    /**
     * Deserializes a byte array to a value.
     *
     * @param bytes The byte array to deserialize
     * @param clazz The class of the value to deserialize to
     * @return The deserialized value
     */
    fun <T : Any> deserialize(bytes: ByteArray, clazz: Class<T>): T

    /**
     * Serializes a cache entry to a byte array.
     *
     * @param entry The cache entry to serialize
     * @return The serialized cache entry as a byte array
     */
    fun <T : Any> serializeEntry(entry: CacheEntry<T>): ByteArray

    /**
     * Deserializes a byte array to a cache entry.
     *
     * @param bytes The byte array to deserialize
     * @param valueClass The class of the value in the cache entry
     * @return The deserialized cache entry
     */
    fun <T : Any> deserializeEntry(bytes: ByteArray, valueClass: Class<T>): CacheEntry<T>

    /**
     * Compresses a byte array.
     *
     * @param bytes The byte array to compress
     * @return The compressed byte array
     */
    fun compress(bytes: ByteArray): ByteArray

    /**
     * Decompresses a byte array.
     *
     * @param bytes The byte array to decompress
     * @return The decompressed byte array
     */
    fun decompress(bytes: ByteArray): ByteArray
}
