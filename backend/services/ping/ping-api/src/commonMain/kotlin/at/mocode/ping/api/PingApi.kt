package at.mocode.ping.api

interface PingApi {
    suspend fun simplePing(): PingResponse
    suspend fun enhancedPing(simulate: Boolean = false): EnhancedPingResponse
    suspend fun healthCheck(): HealthResponse
}
