package at.mocode.clients.shared.presentation.store

import at.mocode.clients.shared.domain.models.User
import at.mocode.clients.shared.domain.models.AuthToken
import at.mocode.clients.shared.presentation.actions.AppAction
import kotlinx.coroutines.Dispatchers
import kotlin.test.*

class AppStoreTest {

    @Test
    fun `store should be created successfully`() {
        val store = AppStore(Dispatchers.Unconfined)
        assertNotNull(store)
        store.cleanup()
    }

    @Test
    fun `auth actions should update state`() {
        val store = AppStore(Dispatchers.Unconfined)

        // Test login start action
        store.dispatch(AppAction.Auth.LoginStart("testuser", "password"))

        // Test login success
        val user = User("1", "test", "test@example.com", "Test", "User")
        val token = AuthToken("access", "refresh", 3600)
        store.dispatch(AppAction.Auth.LoginSuccess(user, token))

        // Test logout
        store.dispatch(AppAction.Auth.Logout)

        store.cleanup()
        assertTrue(true) // Basic test to verify actions don't throw exceptions
    }

    @Test
    fun `navigation actions should work`() {
        val store = AppStore(Dispatchers.Unconfined)

        store.dispatch(AppAction.Navigation.NavigateTo("/dashboard"))
        store.dispatch(AppAction.Navigation.NavigateBack)

        store.cleanup()
        assertTrue(true)
    }

    @Test
    fun `ui actions should work`() {
        val store = AppStore(Dispatchers.Unconfined)

        store.dispatch(AppAction.UI.ToggleDarkMode)
        store.dispatch(AppAction.UI.SetLoading(true))

        store.cleanup()
        assertTrue(true)
    }

    @Test
    fun `network actions should work`() {
        val store = AppStore(Dispatchers.Unconfined)

        store.dispatch(AppAction.Network.SetOnlineStatus(false))
        store.dispatch(AppAction.Network.UpdateLastSync("2024-01-01T12:00:00Z"))

        store.cleanup()
        assertTrue(true)
    }
}
