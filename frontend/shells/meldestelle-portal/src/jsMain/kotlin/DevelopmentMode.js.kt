actual fun isDevelopmentMode(): Boolean =
  kotlinx.browser.window.location.hostname == "localhost"
