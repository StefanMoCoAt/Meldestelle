package at.mocode.ping.domain

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import java.time.Instant

/**
 * Secondary Port (Outbound Port).
 * Definiert, wie Pings gespeichert werden, ohne die Technologie (DB) zu kennen.
 */
@OptIn(ExperimentalUuidApi::class)
interface PingRepository {
    fun save(ping: Ping): Ping
    fun findAll(): List<Ping>
    fun findById(id: Uuid): Ping?
    fun findByTimestampAfter(timestamp: Instant): List<Ping>
}
