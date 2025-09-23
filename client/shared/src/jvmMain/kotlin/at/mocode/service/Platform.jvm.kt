package at.mocode.service

// Use direct ping-service for JVM (Desktop) - based on central.toml
actual fun getBaseUrl(): String = "http://localhost:8082"
