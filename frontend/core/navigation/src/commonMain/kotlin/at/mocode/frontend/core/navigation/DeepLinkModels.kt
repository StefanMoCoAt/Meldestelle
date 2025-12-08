package at.mocode.frontend.core.navigation

data class DeepLink(
  val type: DeepLinkType,
  val route: String,
  val originalUrl: String,
)

enum class DeepLinkType {
  CUSTOM_SCHEME,
  WEB_LINK,
}
