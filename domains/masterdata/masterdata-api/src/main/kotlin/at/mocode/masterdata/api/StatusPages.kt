package at.mocode.masterdata.api

import at.mocode.core.domain.model.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

// Eine einfache, eigene Exception, um "Nicht gefunden"-Fälle klarer zu machen.
class NotFoundException(message: String) : RuntimeException(message)

fun Application.configureStatusPages() {
    install(StatusPages) {

        // Regel 1: Fange alle "IllegalArgumentException" ab.
        // Das passiert bei ungültigen Eingaben, z.B. ein falsches UUID-Format.
        exception<IllegalArgumentException> { call, cause ->
            log.warn("Bad Request: ${cause.message}")
            val errorResponse = ApiResponse<Unit>(
                message = cause.message ?: "Invalid input provided.",
                errors = listOf("BAD_REQUEST")
            )
            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }

        // Regel 2: Fange unsere eigene "NotFoundException" ab.
        // Diese werfen wir, wenn eine Entität nicht in der DB gefunden wurde.
        exception<NotFoundException> { call, cause ->
            log.info("Resource not found: ${cause.message}")
            val errorResponse = ApiResponse<Unit>(
                message = cause.message ?: "The requested resource was not found.",
                errors = listOf("NOT_FOUND")
            )
            call.respond(HttpStatusCode.NotFound, errorResponse)
        }

        // Regel 3: Fange alle anderen, unerwarteten Fehler ab.
        // Das ist unser Sicherheitsnetz für alles, was wir nicht vorhergesehen haben.
        exception<Throwable> { call, cause ->
            log.error("Internal Server Error", cause) // Logge den kompletten Stacktrace
            val errorResponse = ApiResponse<Unit>(
                message = "An unexpected internal server error occurred.",
                errors = listOf("INTERNAL_SERVER_ERROR")
            )
            call.respond(HttpStatusCode.InternalServerError, errorResponse)
        }
    }
}
