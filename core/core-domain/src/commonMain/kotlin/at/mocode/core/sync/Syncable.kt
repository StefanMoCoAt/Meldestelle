package at.mocode.core.sync

/**
 * Shared sync contract for all platforms.
 *
 * IMPORTANT: This lives in core (not frontend) so that `:contracts:*` can depend on it.
 */
interface Syncable {
  /** Eindeutige ID der Entität (UUID/UUIDv7 als String). */
  val id: String

  /**
   * Letzter Änderungszeitpunkt der Entität.
   * Konvention: `Long` (epoch millis) oder ein kompatibler, monotoner Zeitstempel.
   */
  val lastModified: Long
}
