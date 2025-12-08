package at.mocode.entries.api

interface EntriesApi {
    suspend fun healthCheck(): HealthResponse
}
