package at.mocode.model

// Deprecated local DTOs are replaced by typealiases to the shared API contract.
// This preserves binary/source compatibility for existing imports while enforcing SSoT.

typealias PingResponse = at.mocode.ping.api.PingResponse

typealias EnhancedPingResponse = at.mocode.ping.api.EnhancedPingResponse

typealias HealthResponse = at.mocode.ping.api.HealthResponse
