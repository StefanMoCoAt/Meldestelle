package at.mocode.frontend.core.navigation

import at.mocode.frontend.core.domain.models.AppRoles
import at.mocode.frontend.core.domain.models.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeNav : NavigationPort {
  var last: String? = null
  override fun navigateTo(route: String) { last = route }
}

private class FakeUserProvider(private val user: User?) : CurrentUserProvider {
  override fun getCurrentUser(): User? = user
}

class DeepLinkHandlerTest {

  @Test
  fun testAdminRouteRejectedForGuest() {
    val nav = FakeNav()
    val provider = FakeUserProvider(null) // guest
    val handler = DeepLinkHandler(nav, provider)

    val ok = handler.handleDeepLink("https://meldestelle.com/admin/dashboard")

    assertTrue(ok)
    assertEquals(Routes.Auth.LOGIN, nav.last, "Guest must be redirected to login for admin route")
  }

  @Test
  fun testAdminRouteAllowedForAdmin() {
    val nav = FakeNav()
    val adminUser = User(id = "1", username = "admin", roles = listOf(AppRoles.ADMIN))
    val provider = FakeUserProvider(adminUser)
    val handler = DeepLinkHandler(nav, provider)

    val ok = handler.handleDeepLink("https://meldestelle.com/admin/settings")

    assertTrue(ok)
    assertEquals("/admin/settings", nav.last, "Admin must be allowed to navigate to admin route")
  }
}
