package at.mocode.shared.navigation

import at.mocode.shared.presentation.state.NavigationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Interface für das Persistieren von Navigation State
 */
interface NavigationPersistence {
  suspend fun saveNavigationState(state: NavigationState)
  fun getNavigationState(): Flow<NavigationState?>
  suspend fun clearNavigationState()
}

/**
 * Default implementation ohne echte Persistierung (In-Memory)
 * Platform-spezifische Implementierungen können echte Persistierung bereitstellen
 */
class DefaultNavigationPersistence : NavigationPersistence {
  private var currentState: NavigationState? = null

  override suspend fun saveNavigationState(state: NavigationState) {
    currentState = state
  }

  override fun getNavigationState(): Flow<NavigationState?> {
    return flowOf(currentState)
  }

  override suspend fun clearNavigationState() {
    currentState = null
  }
}

/**
 * Navigation History Manager mit Persistierung
 */
class NavigationHistoryManager(
  private val persistence: NavigationPersistence
) {
  companion object {
    private const val MAX_HISTORY_SIZE = 50
  }

  suspend fun saveRoute(route: String, history: List<String>) {
    val state = NavigationState(
      currentRoute = route,
      history = history.takeLast(MAX_HISTORY_SIZE),
      canGoBack = history.isNotEmpty()
    )
    persistence.saveNavigationState(state)
  }

  fun getPersistedState() = persistence.getNavigationState()

  suspend fun clear() = persistence.clearNavigationState()

  /**
   * Optimiert die History für bessere Performance
   */
  private fun optimizeHistory(history: List<String>): List<String> {
    // Entfernt Duplikate in Folge und behält nur die letzten N Einträge
    return history
      .fold(emptyList<String>()) { acc, route ->
        if (acc.lastOrNull() != route) acc + route else acc
      }
      .takeLast(MAX_HISTORY_SIZE)
  }

  suspend fun addToHistory(newRoute: String, currentHistory: List<String>) {
    val optimizedHistory = optimizeHistory(currentHistory + newRoute)
    saveRoute(newRoute, optimizedHistory.dropLast(1))
  }
}
