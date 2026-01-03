package at.mocode.infrastructure.gateway.error

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Einfacher ProblemDetails-Handler für unerwartete Fehler im Gateway.
 * Gibt application/problem+json zurück mit Correlation-ID als traceId.
 */
@Component
class ProblemDetailsExceptionHandler : ErrorWebExceptionHandler {

  private val logger = LoggerFactory.getLogger(ProblemDetailsExceptionHandler::class.java)
  private val mapper = ObjectMapper()

  override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
    // Versuche, Status aus Attributen zu lesen, ansonsten 500
    val status = exchange.response.statusCode?.value() ?: HttpStatus.INTERNAL_SERVER_ERROR.value()
    val traceId = exchange.request.headers.getFirst("X-Correlation-ID")

    logger.error("Gateway error [{}]: {} (TraceId: {})", status, ex.message, traceId, ex)

    val body = mapOf(
      "type" to "about:blank",
      "title" to (ex.message ?: "Unexpected error"),
      "status" to status,
      "traceId" to traceId
    )

    exchange.response.statusCode = HttpStatus.valueOf(status)
    exchange.response.headers.contentType = MediaType.APPLICATION_PROBLEM_JSON

    val bytes = mapper.writeValueAsBytes(body)
    val buffer = exchange.response.bufferFactory().wrap(bytes)
    return exchange.response.writeWith(Mono.just(buffer))
  }
}
