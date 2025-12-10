package at.mocode.clients.authfeature.oauth

import kotlinx.browser.window

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object AuthCallbackParams {
  actual fun parse(): CallbackParams? {
    val search = window.location.search
    if (search.isBlank()) return null
    val params = js("new URLSearchParams(arguments[0])").unsafeCast<(String) -> dynamic>()(search)
    val code = params.get("code") as String?
    val state = params.get("state") as String?
    return if (!code.isNullOrBlank()) {
      // Clean up query params to avoid re-processing on recomposition
      val url = window.location.origin + window.location.pathname
      window.history.replaceState(null, "", url)
      CallbackParams(code, state)
    } else null
  }
}
