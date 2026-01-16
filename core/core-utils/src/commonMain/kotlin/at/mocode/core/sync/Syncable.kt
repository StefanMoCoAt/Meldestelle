package at.mocode.core.sync

/**
 * Interface for entities that can be synchronized.
 */
interface Syncable {
    val id: String
    val lastModified: Long
}
