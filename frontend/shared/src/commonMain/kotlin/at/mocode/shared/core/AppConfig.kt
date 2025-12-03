package at.mocode.shared.core

data class AppConfig(
  val gatewayUrl: String,
  val isDebug: Boolean
)

// Standard-Config für Local Development
val devConfig = AppConfig(
  gatewayUrl = "http://localhost:8081",
  isDebug = true
)
