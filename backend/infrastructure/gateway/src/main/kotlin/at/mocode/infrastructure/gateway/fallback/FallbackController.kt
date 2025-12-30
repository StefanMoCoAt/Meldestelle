package at.mocode.infrastructure.gateway.fallback

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * Alternative FallbackController (deaktiviert per Default), nur aktivierbar über
 * property `gateway.customFallback.enabled=true`. Standardmäßig existiert bereits
 * ein FallbackController unter `...gateway.controller.FallbackController`.
 */
@RestController
@ConditionalOnProperty(prefix = "gateway.customFallback", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class FallbackController {

  @RequestMapping("/fallback/ping")
  fun pingFallback(): ResponseEntity<Map<String, Any>> =
    ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
      mapOf(
        "message" to "Ping service unavailable",
        "timestamp" to Instant.now().toString()
      )
    )
}
