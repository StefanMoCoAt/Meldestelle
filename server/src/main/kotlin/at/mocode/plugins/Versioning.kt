package at.mocode.plugins

import at.mocode.dto.base.VersionManager
import at.mocode.dto.base.VersionValidationResult
import at.mocode.dto.base.VersionedDto
import at.mocode.dto.base.VersionedResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

/**
 * Plugin for handling API versioning
 */
val VersioningPlugin = createApplicationPlugin(name = "VersioningPlugin") {

    onCall { call ->
        // Extract version from headers
        val clientVersion = call.request.header("API-Version")
            ?: call.request.header("X-API-Version")
            ?: VersionManager.CURRENT_API_VERSION

        // Validate version
        when (val result = VersionManager.validateClientVersion(clientVersion)) {
            is VersionValidationResult.Valid -> {
                call.attributes.put(ClientVersionKey, result.version)
            }
            is VersionValidationResult.DeprecatedVersion -> {
                call.attributes.put(ClientVersionKey, result.version)
                call.response.header("X-API-Version-Warning", "Version ${result.version} is deprecated")
            }
            is VersionValidationResult.UnsupportedVersion -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "error" to "Unsupported API version: ${result.version}",
                        "supportedVersions" to VersionManager.SUPPORTED_VERSIONS,
                        "currentVersion" to VersionManager.CURRENT_API_VERSION
                    )
                )
                return@onCall
            }
            is VersionValidationResult.MissingVersion -> {
                call.attributes.put(ClientVersionKey, VersionManager.CURRENT_API_VERSION)
            }
        }

        // Add version info to response headers
        call.response.header("API-Version", VersionManager.CURRENT_API_VERSION)
        call.response.header("X-Supported-Versions", VersionManager.SUPPORTED_VERSIONS.joinToString(","))
    }
}

/**
 * Key for storing client version in call attributes
 */
val ClientVersionKey = AttributeKey<String>("ClientVersion")

/**
 * Extension function to get client version from call
 */
fun ApplicationCall.getClientVersion(): String {
    return attributes.getOrNull(ClientVersionKey) ?: VersionManager.CURRENT_API_VERSION
}

/**
 * Extension function to respond with versioned data
 */
suspend inline fun <reified T : VersionedDto> ApplicationCall.respondVersioned(
    status: HttpStatusCode = HttpStatusCode.OK,
    data: T
) {
    val versionedResponse = VersionedResponse(
        data = data,
        version = VersionManager.getVersionInfo(),
        timestamp = Clock.System.now().toString()
    )
    respond(status, versionedResponse)
}

/**
 * Extension function to respond with versioned list data
 */
suspend inline fun <reified T : VersionedDto> ApplicationCall.respondVersionedList(
    status: HttpStatusCode = HttpStatusCode.OK,
    data: List<T>
) {
    val response = mapOf(
        "items" to data,
        "count" to data.size,
        "version" to VersionManager.getVersionInfo(),
        "timestamp" to Clock.System.now().toString()
    )
    respond(status, response)
}

/**
 * Configure versioning for the application
 */
fun Application.configureVersioning() {
    install(VersioningPlugin)
}
