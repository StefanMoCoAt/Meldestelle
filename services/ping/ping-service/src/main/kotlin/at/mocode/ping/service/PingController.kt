package at.mocode.ping.service

import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingResponse
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@RestController
@CrossOrigin(
    origins = ["http://localhost:8080", "http://localhost:8083", "http://localhost:4000"],
    methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS],
    allowedHeaders = ["*"],
    allowCredentials = "true"
)
class PingController(
    private val pingService: PingServiceCircuitBreaker
) : PingApi {

    // Contract endpoints
    @GetMapping("/api/ping/simple")
    override suspend fun simplePing(): PingResponse {
        val now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return PingResponse(
            status = "pong",
            timestamp = now,
            service = "ping-service"
        )
    }

    @GetMapping("/api/ping/enhanced")
    override suspend fun enhancedPing(
        @RequestParam(required = false, defaultValue = "false") simulate: Boolean
    ): EnhancedPingResponse = pingService.ping(simulate)

    @GetMapping("/api/ping/health")
    override suspend fun healthCheck(): HealthResponse = pingService.healthCheck()
}
