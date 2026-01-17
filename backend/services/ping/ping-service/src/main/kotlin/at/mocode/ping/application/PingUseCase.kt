package at.mocode.ping.application

import at.mocode.ping.domain.Ping
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Primary Port (Inbound Port).
 * Definiert die fachlichen Operationen, die von außen (Controller) aufgerufen werden können.
 */
@OptIn(ExperimentalUuidApi::class)
interface PingUseCase {
    fun executePing(message: String): Ping
    fun getPingHistory(): List<Ping>
    fun getPing(id: Uuid): Ping?
    fun getPingsSince(timestamp: Long): List<Ping>
}
