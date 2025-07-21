package at.mocode.gateway.config

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import at.mocode.enums.RolleE
import at.mocode.enums.BerechtigungE

/**
 * Authorization configuration and middleware for role-based access control.
 *
 * Provides utilities for checking user roles and permissions on protected endpoints.
 */

/**
 * Enum representing user roles in the system.
 */
enum class UserRole {
    ADMIN,
    VEREINS_ADMIN,
    FUNKTIONAER,
    REITER,
    TRAINER,
    RICHTER,
    TIERARZT,
    ZUSCHAUER,
    GAST
}

/**
 * Enum representing permissions in the system.
 */
enum class Permission {
    // Person management
    PERSON_READ,
    PERSON_CREATE,
    PERSON_UPDATE,
    PERSON_DELETE,

    // Club management
    VEREIN_READ,
    VEREIN_CREATE,
    VEREIN_UPDATE,
    VEREIN_DELETE,

    // Event management
    VERANSTALTUNG_READ,
    VERANSTALTUNG_CREATE,
    VERANSTALTUNG_UPDATE,
    VERANSTALTUNG_DELETE,

    // Horse management
    PFERD_READ,
    PFERD_CREATE,
    PFERD_UPDATE,
    PFERD_DELETE,

    // Master data management
    STAMMDATEN_READ,
    STAMMDATEN_UPDATE,

    // System administration
    SYSTEM_ADMIN,
    BENUTZER_VERWALTEN,
    ROLLEN_VERWALTEN
}

/**
 * Data class representing user authorization context.
 */
data class UserAuthContext(
    val userId: String,
    val username: String,
    val roles: List<UserRole>,
    val permissions: List<Permission>
)

/**
 * Maps domain role enum to authorization role enum.
 */
private fun mapDomainRoleToUserRole(domainRole: RolleE): UserRole {
    return when (domainRole) {
        RolleE.ADMIN -> UserRole.ADMIN
        RolleE.VEREINS_ADMIN -> UserRole.VEREINS_ADMIN
        RolleE.FUNKTIONAER -> UserRole.FUNKTIONAER
        RolleE.REITER -> UserRole.REITER
        RolleE.TRAINER -> UserRole.TRAINER
        RolleE.RICHTER -> UserRole.RICHTER
        RolleE.TIERARZT -> UserRole.TIERARZT
        RolleE.ZUSCHAUER -> UserRole.ZUSCHAUER
        RolleE.GAST -> UserRole.GAST
    }
}

/**
 * Maps domain permission enum to authorization permission enum.
 */
private fun mapDomainPermissionToPermission(domainPermission: BerechtigungE): Permission {
    return when (domainPermission) {
        BerechtigungE.PERSON_READ -> Permission.PERSON_READ
        BerechtigungE.PERSON_CREATE -> Permission.PERSON_CREATE
        BerechtigungE.PERSON_UPDATE -> Permission.PERSON_UPDATE
        BerechtigungE.PERSON_DELETE -> Permission.PERSON_DELETE
        BerechtigungE.VEREIN_READ -> Permission.VEREIN_READ
        BerechtigungE.VEREIN_CREATE -> Permission.VEREIN_CREATE
        BerechtigungE.VEREIN_UPDATE -> Permission.VEREIN_UPDATE
        BerechtigungE.VEREIN_DELETE -> Permission.VEREIN_DELETE
        BerechtigungE.VERANSTALTUNG_READ -> Permission.VERANSTALTUNG_READ
        BerechtigungE.VERANSTALTUNG_CREATE -> Permission.VERANSTALTUNG_CREATE
        BerechtigungE.VERANSTALTUNG_UPDATE -> Permission.VERANSTALTUNG_UPDATE
        BerechtigungE.VERANSTALTUNG_DELETE -> Permission.VERANSTALTUNG_DELETE
        BerechtigungE.PFERD_READ -> Permission.PFERD_READ
        BerechtigungE.PFERD_CREATE -> Permission.PFERD_CREATE
        BerechtigungE.PFERD_UPDATE -> Permission.PFERD_UPDATE
        BerechtigungE.PFERD_DELETE -> Permission.PFERD_DELETE
        BerechtigungE.STAMMDATEN_READ -> Permission.STAMMDATEN_READ
        BerechtigungE.STAMMDATEN_UPDATE -> Permission.STAMMDATEN_UPDATE
        BerechtigungE.SYSTEM_ADMIN -> Permission.SYSTEM_ADMIN
        BerechtigungE.BENUTZER_VERWALTEN -> Permission.BENUTZER_VERWALTEN
        BerechtigungE.ROLLEN_VERWALTEN -> Permission.ROLLEN_VERWALTEN
    }
}

/**
 * Extension function to get user authorization context from JWT principal.
 */
fun JWTPrincipal.getUserAuthContext(): UserAuthContext? {
    val userId = getClaim("userId", String::class) ?: return null
    val username = getClaim("username", String::class) ?: return null

    // Get roles and permissions from JWT token
    val domainRoles = getClaim("roles", Array<RolleE>::class)?.toList() ?: emptyList()
    val domainPermissions = getClaim("permissions", Array<BerechtigungE>::class)?.toList() ?: emptyList()

    // Map domain enums to authorization enums
    val roles = domainRoles.map { mapDomainRoleToUserRole(it) }
    val permissions = domainPermissions.map { mapDomainPermissionToPermission(it) }

    return UserAuthContext(
        userId = userId,
        username = username,
        roles = roles,
        permissions = permissions
    )
}

/**
 * Maps roles to their corresponding permissions.
 */
private fun getRolePermissions(roles: List<UserRole>): List<Permission> {
    val permissions = mutableSetOf<Permission>()

    roles.forEach { role ->
        when (role) {
            UserRole.ADMIN -> {
                permissions.addAll(Permission.values())
            }
            UserRole.VEREINS_ADMIN -> {
                permissions.addAll(listOf(
                    Permission.PERSON_READ, Permission.PERSON_CREATE, Permission.PERSON_UPDATE,
                    Permission.VEREIN_READ, Permission.VEREIN_UPDATE,
                    Permission.PFERD_READ, Permission.PFERD_CREATE, Permission.PFERD_UPDATE,
                    Permission.STAMMDATEN_READ
                ))
            }
            UserRole.FUNKTIONAER -> {
                permissions.addAll(listOf(
                    Permission.PERSON_READ,
                    Permission.VEREIN_READ,
                    Permission.VERANSTALTUNG_READ, Permission.VERANSTALTUNG_CREATE, Permission.VERANSTALTUNG_UPDATE,
                    Permission.PFERD_READ,
                    Permission.STAMMDATEN_READ
                ))
            }
            UserRole.TRAINER -> {
                permissions.addAll(listOf(
                    Permission.PERSON_READ,
                    Permission.VEREIN_READ,
                    Permission.VERANSTALTUNG_READ,
                    Permission.PFERD_READ,
                    Permission.STAMMDATEN_READ
                ))
            }
            UserRole.REITER -> {
                permissions.addAll(listOf(
                    Permission.PERSON_READ,
                    Permission.VEREIN_READ,
                    Permission.VERANSTALTUNG_READ,
                    Permission.PFERD_READ,
                    Permission.STAMMDATEN_READ
                ))
            }
            UserRole.RICHTER -> {
                permissions.addAll(listOf(
                    Permission.PERSON_READ,
                    Permission.VEREIN_READ,
                    Permission.VERANSTALTUNG_READ,
                    Permission.PFERD_READ,
                    Permission.STAMMDATEN_READ
                ))
            }
            UserRole.TIERARZT -> {
                permissions.addAll(listOf(
                    Permission.PERSON_READ,
                    Permission.PFERD_READ,
                    Permission.STAMMDATEN_READ
                ))
            }
            UserRole.ZUSCHAUER -> {
                permissions.addAll(listOf(
                    Permission.VERANSTALTUNG_READ,
                    Permission.STAMMDATEN_READ
                ))
            }
            UserRole.GAST -> {
                permissions.addAll(listOf(
                    Permission.STAMMDATEN_READ
                ))
            }
        }
    }

    return permissions.toList()
}

/**
 * Route extension function to require specific roles.
 */
fun Route.requireRoles(vararg roles: UserRole, build: Route.() -> Unit): Route {
    val route = createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
            return RouteSelectorEvaluation.Constant
        }

        override fun toString(): String = "requireRoles(${roles.joinToString()})"
    })

    route.intercept(ApplicationCallPipeline.Call) {
        val principal = call.principal<JWTPrincipal>()
        val authContext = principal?.getUserAuthContext()

        if (authContext == null) {
            call.respond(HttpStatusCode.Unauthorized, "Authentication required")
            finish()
            return@intercept
        }

        val hasRequiredRole = roles.any { requiredRole ->
            authContext.roles.contains(requiredRole)
        }

        if (!hasRequiredRole) {
            call.respond(
                HttpStatusCode.Forbidden,
                "Access denied. Required roles: ${roles.joinToString()}"
            )
            finish()
            return@intercept
        }
    }

    route.build()
    return route
}

/**
 * Route extension function to require specific permissions.
 */
fun Route.requirePermissions(vararg permissions: Permission, build: Route.() -> Unit): Route {
    val route = createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
            return RouteSelectorEvaluation.Constant
        }

        override fun toString(): String = "requirePermissions(${permissions.joinToString()})"
    })

    route.intercept(ApplicationCallPipeline.Call) {
        val principal = call.principal<JWTPrincipal>()
        val authContext = principal?.getUserAuthContext()

        if (authContext == null) {
            call.respond(HttpStatusCode.Unauthorized, "Authentication required")
            finish()
            return@intercept
        }

        val hasAllPermissions = permissions.all { requiredPermission ->
            authContext.permissions.contains(requiredPermission)
        }

        if (!hasAllPermissions) {
            call.respond(
                HttpStatusCode.Forbidden,
                "Access denied. Required permissions: ${permissions.joinToString()}"
            )
            finish()
            return@intercept
        }
    }

    route.build()
    return route
}

/**
 * Pipeline context extension to get current user authorization context.
 */
val PipelineContext<Unit, ApplicationCall>.userAuthContext: UserAuthContext?
    get() = call.principal<JWTPrincipal>()?.getUserAuthContext()

/**
 * Application call extension to check if user has specific role.
 */
fun ApplicationCall.hasRole(role: UserRole): Boolean {
    val authContext = principal<JWTPrincipal>()?.getUserAuthContext()
    return authContext?.roles?.contains(role) == true
}

/**
 * Application call extension to check if user has specific permission.
 */
fun ApplicationCall.hasPermission(permission: Permission): Boolean {
    val authContext = principal<JWTPrincipal>()?.getUserAuthContext()
    return authContext?.permissions?.contains(permission) == true
}
