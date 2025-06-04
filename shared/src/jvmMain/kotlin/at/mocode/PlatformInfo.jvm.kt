package at.mocode

import at.mocode.API_HOST

/**
 * JVM implementation of PlatformInfo.
 * Provides platform-specific information for the JVM target.
 */
actual object PlatformInfo {
    /**
     * Returns the API host for JVM applications.
     * Uses the API_HOST constant defined in Constants.kt.
     */
    actual val apiHost: String
        get() = API_HOST
}
