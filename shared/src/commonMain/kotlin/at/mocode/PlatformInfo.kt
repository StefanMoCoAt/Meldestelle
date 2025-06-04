package at.mocode

/**
 * Platform-specific information provider.
 * This expect class is implemented differently for each target platform.
 */
expect object PlatformInfo {
    /**
     * The host address for API calls.
     * This will be different depending on the platform:
     * - In JVM: Uses the API_HOST constant
     * - In WebAssembly JS: Uses API_HOST in production, 'localhost' in development
     */
    val apiHost: String
}
