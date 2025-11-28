package at.mocode.clients.authfeature.oauth

data class CallbackParams(val code: String, val state: String?)

expect object AuthCallbackParams {
    /**
     * Parse OAuth callback parameters from the current environment.
     * - JS (web): reads window.location.search for `code` and `state` and removes them from the URL.
     * - JVM (desktop): returns null.
     */
    fun parse(): CallbackParams?
}
