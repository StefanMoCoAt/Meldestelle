actual fun isDevelopmentMode(): Boolean =
  System.getProperty("development.mode", "false").toBoolean()
