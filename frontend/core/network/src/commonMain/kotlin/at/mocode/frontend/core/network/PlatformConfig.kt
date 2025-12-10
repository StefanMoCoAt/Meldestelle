package at.mocode.frontend.core.network

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PlatformConfig {
  fun resolveApiBaseUrl(): String
}
