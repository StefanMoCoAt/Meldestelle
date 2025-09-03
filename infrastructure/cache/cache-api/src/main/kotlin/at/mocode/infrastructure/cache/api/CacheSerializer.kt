package at.mocode.infrastructure.cache.api

/**
 * Schnittstelle zum Serialisieren und Deserialisieren von Cache-Einträgen.
 */
interface CacheSerializer {
    /**
     * Serialisiert einen Wert zu einem Byte-Array.
     *
     * @param value Der zu serialisierende Wert
     * @return Der serialisierte Wert als Byte-Array
     */
    fun <T : Any> serialize(value: T): ByteArray

    /**
     * Deserialisiert ein Byte-Array zu einem Wert.
     *
     * @param bytes Das zu deserialisierende Byte-Array
     * @param clazz Die Zielklasse des zu deserialisierenden Werts
     * @return Der deserialisierte Wert
     */
    fun <T : Any> deserialize(bytes: ByteArray, clazz: Class<T>): T

    /**
     * Serialisiert einen Cache-Eintrag zu einem Byte-Array.
     *
     * @param entry Der zu serialisierende Cache-Eintrag
     * @return Der serialisierte Cache-Eintrag als Byte-Array
     */
    fun <T : Any> serializeEntry(entry: CacheEntry<T>): ByteArray

    /**
     * Deserialisiert ein Byte-Array zu einem Cache-Eintrag.
     *
     * @param bytes Das zu deserialisierende Byte-Array
     * @param valueClass Die Klasse des Werts im Cache-Eintrag
     * @return Der deserialisierte Cache-Eintrag
     */
    fun <T : Any> deserializeEntry(bytes: ByteArray, valueClass: Class<T>): CacheEntry<T>

    /**
     * Komprimiert ein Byte-Array.
     *
     * @param bytes Das zu komprimierende Byte-Array
     * @return Das komprimierte Byte-Array
     */
    fun compress(bytes: ByteArray): ByteArray

    /**
     * Dekomprimiert ein Byte-Array.
     *
     * @param bytes Das zu dekomprimierende Byte-Array
     * @return Das dekomprimierte Byte-Array
     */
    fun decompress(bytes: ByteArray): ByteArray
}
