package at.mocode.shared.presentation.store

import at.mocode.shared.presentation.state.AppState
import at.mocode.shared.presentation.actions.AppAction
import at.mocode.shared.presentation.state.AuthState
import at.mocode.shared.presentation.state.NavigationState
import at.mocode.shared.presentation.state.NetworkState
import at.mocode.shared.presentation.state.UiState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AppStore(
  private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) {
  private val scope = CoroutineScope(SupervisorJob() + dispatcher)
  private val _state = MutableStateFlow(AppState())

  val state: StateFlow<AppState> = _state.asStateFlow()

  fun dispatch(action: AppAction) {
    scope.launch {
      val currentState = _state.value
      val newState = reduce(currentState, action)
      _state.value = newState

      // Handle side effects
      handleSideEffect(action, newState)
    }
  }

  private fun reduce(currentState: AppState, action: AppAction): AppState {
    return when (action) {
      is AppAction.Auth -> currentState.copy(
        auth = reduceAuth(currentState.auth, action)
      )

      is AppAction.Navigation -> currentState.copy(
        navigation = reduceNavigation(currentState.navigation, action)
      )

      is AppAction.UI -> currentState.copy(
        ui = reduceUI(currentState.ui, action)
      )

      is AppAction.Network -> currentState.copy(
        network = reduceNetwork(currentState.network, action)
      )
    }
  }

  private fun reduceAuth(currentAuth: AuthState, action: AppAction.Auth): AuthState {
    return when (action) {
      is AppAction.Auth.LoginStart -> currentAuth.copy(
        isLoading = true,
        error = null
      )

      is AppAction.Auth.LoginSuccess -> currentAuth.copy(
        isAuthenticated = true,
        user = action.user,
        token = action.token,
        isLoading = false,
        error = null
      )

      is AppAction.Auth.LoginFailure -> currentAuth.copy(
        isAuthenticated = false,
        user = null,
        token = null,
        isLoading = false,
        error = action.error
      )

      is AppAction.Auth.Logout -> AuthState()
      is AppAction.Auth.RefreshToken -> currentAuth.copy(
        token = action.newToken
      )
    }
  }

  private fun reduceNavigation(currentNav: NavigationState, action: AppAction.Navigation): NavigationState {
    return when (action) {
      is AppAction.Navigation.NavigateTo -> currentNav.copy(
        currentRoute = action.route,
        history = currentNav.history + currentNav.currentRoute,
        canGoBack = true
      )

      is AppAction.Navigation.NavigateBack -> {
        val newHistory = currentNav.history.dropLast(1)
        currentNav.copy(
          currentRoute = newHistory.lastOrNull() ?: "/",
          history = newHistory,
          canGoBack = newHistory.isNotEmpty()
        )
      }

      is AppAction.Navigation.UpdateHistory -> currentNav.copy(
        currentRoute = action.route
      )
    }
  }

  private fun reduceUI(currentUI: UiState, action: AppAction.UI): UiState {
    return when (action) {
      is AppAction.UI.ToggleDarkMode -> currentUI.copy(
        isDarkMode = !currentUI.isDarkMode
      )

      is AppAction.UI.SetLoading -> currentUI.copy(
        isLoading = action.isLoading
      )

      is AppAction.UI.ShowNotification -> currentUI.copy(
        notifications = currentUI.notifications + action.notification
      )

      is AppAction.UI.DismissNotification -> currentUI.copy(
        notifications = currentUI.notifications.filter { it.id != action.id }
      )
    }
  }

  private fun reduceNetwork(currentNetwork: NetworkState, action: AppAction.Network): NetworkState {
    return when (action) {
      is AppAction.Network.SetOnlineStatus -> currentNetwork.copy(
        isOnline = action.isOnline
      )

      is AppAction.Network.UpdateLastSync -> currentNetwork.copy(
        lastSync = action.timestamp
      )
    }
  }

  private suspend fun handleSideEffect(action: AppAction, newState: AppState) {
    when (action) {
      is AppAction.Auth.LoginSuccess -> {
        // Auto-save token to local storage
        // TODO: Implement storage
      }

      is AppAction.Auth.Logout -> {
        // Clear local storage
        // TODO: Implement storage cleanup
      }

      else -> { /* No side effects */
      }
    }
  }

  fun cleanup() {
    scope.cancel()
  }
}
