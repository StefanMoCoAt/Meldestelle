package at.mocode.frontend.core.navigation

import at.mocode.frontend.core.domain.models.User

/**
 * Abstraction to obtain the current authenticated user (or null if guest).
 * Implementations live in shells/apps and provide access to the actual auth state.
 */
interface CurrentUserProvider {
  fun getCurrentUser(): User?
}
