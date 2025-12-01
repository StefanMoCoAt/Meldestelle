package at.mocode.infrastructure.cache.api

import kotlin.time.Instant

enum class ConnectionState {
    CONNECTED, DISCONNECTED, RECONNECTING
}

interface ConnectionStatusTracker {
    fun getConnectionState(): ConnectionState
    fun getLastStateChangeTime(): Instant
    fun registerConnectionListener(listener: ConnectionStateListener)
    fun unregisterConnectionListener(listener: ConnectionStateListener)
    fun isConnected(): Boolean = getConnectionState() == ConnectionState.CONNECTED
}

interface ConnectionStateListener {
    fun onConnectionStateChanged(newState: ConnectionState, timestamp: Instant)
}
