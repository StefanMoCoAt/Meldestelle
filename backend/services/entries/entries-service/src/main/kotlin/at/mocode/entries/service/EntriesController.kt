package at.mocode.entries.service

import at.mocode.entries.api.EnhancedEntriesResponse
import at.mocode.entries.api.EntriesApi
import at.mocode.entries.api.HealthResponse
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(
  origins = ["http://localhost:8080", "http://localhost:8083", "http://localhost:4000"],
  methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS],
  allowedHeaders = ["*"],
  allowCredentials = "true"
)
class EntriesController(
  private val entriesService: EntriesServiceCircuitBreaker
) : EntriesApi {

  // Contract endpoints
  @GetMapping("/entries/enhanced")
  suspend fun enhancedEntries(
    @RequestParam(required = false, defaultValue = "false") simulate: Boolean
  ): EnhancedEntriesResponse = entriesService.entries(simulate)

  @GetMapping("/entries/health")
  override suspend fun healthCheck(): HealthResponse = entriesService.healthCheck()
}
