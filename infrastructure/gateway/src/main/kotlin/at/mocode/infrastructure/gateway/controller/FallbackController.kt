package at.mocode.infrastructure.gateway.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * Fallback Controller für Circuit Breaker Szenarien.
 * Bietet standardisierte Fehlermeldungen, wenn Backend-Services nicht verfügbar sind.
 */
@RestController
@RequestMapping("/fallback")
class FallbackController {

    @RequestMapping(value = ["/members"], method = [RequestMethod.GET, RequestMethod.POST])
    fun membersFallback(): ResponseEntity<ErrorResponse> {
        return createFallbackResponse("members-service", "Member operations are temporarily unavailable")
    }

    @RequestMapping(value = ["/horses"], method = [RequestMethod.GET, RequestMethod.POST])
    fun horsesFallback(): ResponseEntity<ErrorResponse> {
        return createFallbackResponse("horses-service", "Horse registry operations are temporarily unavailable")
    }

    @RequestMapping(value = ["/events"], method = [RequestMethod.GET, RequestMethod.POST])
    fun eventsFallback(): ResponseEntity<ErrorResponse> {
        return createFallbackResponse("events-service", "Event management operations are temporarily unavailable")
    }

    @RequestMapping(value = ["/masterdata"], method = [RequestMethod.GET, RequestMethod.POST])
    fun masterdataFallback(): ResponseEntity<ErrorResponse> {
        return createFallbackResponse("masterdata-service", "Master data operations are temporarily unavailable")
    }

    @RequestMapping(value = ["/auth"], method = [RequestMethod.GET, RequestMethod.POST])
    fun authFallback(): ResponseEntity<ErrorResponse> {
        return createFallbackResponse("auth-service", "Authentication operations are temporarily unavailable")
    }

    @RequestMapping(value = [""], method = [RequestMethod.GET, RequestMethod.POST])
    fun defaultFallback(): ResponseEntity<ErrorResponse> {
        return createFallbackResponse("unknown-service", "Service is temporarily unavailable")
    }

    private fun createFallbackResponse(service: String, message: String): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            error = "SERVICE_UNAVAILABLE",
            message = message,
            service = service,
            timestamp = LocalDateTime.now(),
            status = HttpStatus.SERVICE_UNAVAILABLE.value(),
            suggestion = "Please try again in a few moments. If the problem persists, contact support."
        )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse)
    }
}

/**
 * Standardisierte Fehlerantwort für Circuit Breaker Fallbacks.
 */
 data class ErrorResponse(
    val error: String,
    val message: String,
    val service: String,
    val timestamp: LocalDateTime,
    val status: Int,
    val suggestion: String
)
