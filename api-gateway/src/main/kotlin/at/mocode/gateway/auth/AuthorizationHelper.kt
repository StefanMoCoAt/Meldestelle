package at.mocode.gateway.auth

import at.mocode.enums.BerechtigungE
import at.mocode.enums.RolleE
import at.mocode.members.domain.service.JwtService
import at.mocode.members.domain.service.UserAuthorizationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import com.benasher44.uuid.Uuid

/**
 * Helper class for authorization checks in API endpoints.
 */
class AuthorizationHelper(
    private val jwtService: JwtService,
    private val userAuthorizationService: UserAuthorizationService
) {

    /**
     * Checks if the current user has the required permission.
     *
     * @param call The application call
     * @param requiredPermission The permission required to access the resource
     * @return true if the user has the permission, false otherwise
     */
    suspend fun hasPermission(call: ApplicationCall, requiredPermission: BerechtigungE): Boolean {
        val principal = call.principal<JWTPrincipal>()
        val userIdString = principal?.subject ?: return false

        val userId = try {
            Uuid.fromString(userIdString)
        } catch (e: Exception) {
            return false
        }

        return userAuthorizationService.hasPermission(userId, requiredPermission)
    }

    /**
     * Checks if the current user has the required role.
     *
     * @param call The application call
     * @param requiredRole The role required to access the resource
     * @return true if the user has the role, false otherwise
     */
    suspend fun hasRole(call: ApplicationCall, requiredRole: RolleE): Boolean {
        val principal = call.principal<JWTPrincipal>()
        val userIdString = principal?.subject ?: return false

        val userId = try {
            Uuid.fromString(userIdString)
        } catch (e: Exception) {
            return false
        }

        return userAuthorizationService.hasRole(userId, requiredRole)
    }

    /**
     * Checks if the current user has any of the required permissions.
     *
     * @param call The application call
     * @param requiredPermissions List of permissions, user needs at least one
     * @return true if the user has at least one of the permissions, false otherwise
     */
    suspend fun hasAnyPermission(call: ApplicationCall, requiredPermissions: List<BerechtigungE>): Boolean {
        val principal = call.principal<JWTPrincipal>()
        val userIdString = principal?.subject ?: return false

        val userId = try {
            Uuid.fromString(userIdString)
        } catch (e: Exception) {
            return false
        }

        val authInfo = userAuthorizationService.getUserAuthInfo(userId) ?: return false
        return authInfo.permissions.any { it in requiredPermissions }
    }

    /**
     * Checks if the current user has any of the required roles.
     *
     * @param call The application call
     * @param requiredRoles List of roles, user needs at least one
     * @return true if the user has at least one of the roles, false otherwise
     */
    suspend fun hasAnyRole(call: ApplicationCall, requiredRoles: List<RolleE>): Boolean {
        val principal = call.principal<JWTPrincipal>()
        val userIdString = principal?.subject ?: return false

        val userId = try {
            Uuid.fromString(userIdString)
        } catch (e: Exception) {
            return false
        }

        val authInfo = userAuthorizationService.getUserAuthInfo(userId) ?: return false
        return authInfo.roles.any { it in requiredRoles }
    }

    /**
     * Gets the current user's ID from the JWT token.
     *
     * @param call The application call
     * @return The user ID if valid, null otherwise
     */
    fun getCurrentUserId(call: ApplicationCall): Uuid? {
        val principal = call.principal<JWTPrincipal>()
        val userIdString = principal?.subject ?: return null

        return try {
            Uuid.fromString(userIdString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Responds with a 403 Forbidden status when authorization fails.
     *
     * @param call The application call
     * @param message Optional custom message
     */
    suspend fun respondForbidden(call: ApplicationCall, message: String = "Insufficient permissions") {
        call.respond(
            HttpStatusCode.Forbidden,
            mapOf("error" to message)
        )
    }

    /**
     * Responds with a 401 Unauthorized status when authentication fails.
     *
     * @param call The application call
     * @param message Optional custom message
     */
    suspend fun respondUnauthorized(call: ApplicationCall, message: String = "Authentication required") {
        call.respond(
            HttpStatusCode.Unauthorized,
            mapOf("error" to message)
        )
    }
}

/**
 * Extension function to check permission and respond with 403 if not authorized.
 */
suspend fun ApplicationCall.requirePermission(
    authHelper: AuthorizationHelper,
    permission: BerechtigungE
): Boolean {
    if (!authHelper.hasPermission(this, permission)) {
        authHelper.respondForbidden(this, "Required permission: ${permission.name}")
        return false
    }
    return true
}

/**
 * Extension function to check role and respond with 403 if not authorized.
 */
suspend fun ApplicationCall.requireRole(
    authHelper: AuthorizationHelper,
    role: RolleE
): Boolean {
    if (!authHelper.hasRole(this, role)) {
        authHelper.respondForbidden(this, "Required role: ${role.name}")
        return false
    }
    return true
}

/**
 * Extension function to check any of the permissions and respond with 403 if not authorized.
 */
suspend fun ApplicationCall.requireAnyPermission(
    authHelper: AuthorizationHelper,
    permissions: List<BerechtigungE>
): Boolean {
    if (!authHelper.hasAnyPermission(this, permissions)) {
        authHelper.respondForbidden(this, "Required permissions: ${permissions.joinToString { it.name }}")
        return false
    }
    return true
}
