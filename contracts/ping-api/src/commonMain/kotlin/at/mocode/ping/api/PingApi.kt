package at.mocode.ping.api

interface PingApi {
    suspend fun simplePing(): PingResponse
    suspend fun enhancedPing(simulate: Boolean = false): EnhancedPingResponse
    suspend fun healthCheck(): HealthResponse

    // Neue Endpunkte für Security Hardening
    suspend fun publicPing(): PingResponse
    suspend fun securePing(): PingResponse

    // Phase 3: Delta-Sync
    suspend fun syncPings(lastSyncTimestamp: Long): List<PingEvent>
}
