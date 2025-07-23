package at.mocode.infrastructure.cache.api

import java.time.Instant

/**
 * Represents the connection status of the cache.
 */
enum class ConnectionState {
    /**
     * The cache is connected to the remote server.
     */
    CONNECTED,

    /**
     * The cache is disconnected from the remote server.
     */
    DISCONNECTED,

    /**
     * The cache is attempting to reconnect to the remote server.
     */
    RECONNECTING
}

/**
 * Interface for tracking the connection status of the cache.
 */
interface ConnectionStatusTracker {
    /**
     * Gets the current connection state.
     *
     * @return The current connection state
     */
    fun getConnectionState(): ConnectionState

    /**
     * Gets the time when the connection state last changed.
     *
     * @return The time when the connection state last changed
     */
    fun getLastStateChangeTime(): Instant

    /**
     * Registers a listener to be notified when the connection state changes.
     *
     * @param listener The listener to register
     */
    fun registerConnectionListener(listener: ConnectionStateListener)

    /**
     * Unregisters a connection state listener.
     *
     * @param listener The listener to unregister
     */
    fun unregisterConnectionListener(listener: ConnectionStateListener)

    /**
     * Checks if the cache is currently connected.
     *
     * @return true if the cache is connected, false otherwise
     */
    fun isConnected(): Boolean = getConnectionState() == ConnectionState.CONNECTED
}

/**
 * Listener for connection state changes.
 */
interface ConnectionStateListener {
    /**
     * Called when the connection state changes.
     *
     * @param newState The new connection state
     * @param timestamp The time when the state changed
     */
    fun onConnectionStateChanged(newState: ConnectionState, timestamp: Instant)
}
