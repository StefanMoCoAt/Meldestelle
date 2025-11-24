package at.mocode.clients.membersfeature.model

import kotlinx.serialization.Serializable

@Serializable
data class MemberProfile(
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val roles: List<String> = emptyList()
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { username ?: "" }
}
