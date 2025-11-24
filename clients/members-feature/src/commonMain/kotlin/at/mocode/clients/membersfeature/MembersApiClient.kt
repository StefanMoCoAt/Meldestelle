package at.mocode.clients.membersfeature

import at.mocode.clients.authfeature.AuthenticatedHttpClient
import at.mocode.clients.shared.AppConfig
import at.mocode.clients.membersfeature.model.MemberProfile
import io.ktor.client.call.*
import io.ktor.client.request.*
import at.mocode.clients.authfeature.AuthenticatedHttpClient.addAuthHeader

class MembersApiClient(
    private val baseUrl: String = AppConfig.GATEWAY_URL
) {
    private val client = AuthenticatedHttpClient.create()

    suspend fun getMyProfile(): MemberProfile {
        // Erwarteter Endpoint: GET /api/members/me
        return client.get("$baseUrl/api/members/me") {
            addAuthHeader()
        }.body()
    }
}
