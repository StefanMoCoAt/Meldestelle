package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.model.DomUser
import at.mocode.members.domain.repository.UserRepository
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * In-memory implementation of UserRepository for testing and development.
 *
 * This implementation provides basic functionality without database persistence.
 * Replace with proper database implementation for production use.
 */
class UserRepositoryImpl : UserRepository {

    private val users = mutableMapOf<Uuid, DomUser>()

    init {
        // Initialize with a test user
        val testUser = DomUser(
            userId = uuid4(),
            personId = uuid4(),
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            salt = "salt123",
            istAktiv = true,
            istEmailVerifiziert = true,
            letzteAnmeldung = null,
            fehlgeschlageneAnmeldungen = 0,
            gesperrtBis = null
        )
        users[testUser.userId] = testUser
    }

    override suspend fun createUser(user: DomUser): DomUser {
        val now = Clock.System.now()
        val updatedUser = user.copy(createdAt = now, updatedAt = now)
        users[updatedUser.userId] = updatedUser
        return updatedUser
    }

    override suspend fun findById(userId: Uuid): DomUser? {
        return users[userId]
    }

    override suspend fun findByUsername(username: String): DomUser? {
        return users.values.find { it.username == username }
    }

    override suspend fun findByEmail(email: String): DomUser? {
        return users.values.find { it.email == email }
    }

    override suspend fun findByPersonId(personId: Uuid): DomUser? {
        return users.values.find { it.personId == personId }
    }

    override suspend fun updateUser(user: DomUser): DomUser {
        val now = Clock.System.now()
        val updatedUser = user.copy(updatedAt = now)
        users[updatedUser.userId] = updatedUser
        return updatedUser
    }

    override suspend fun updateLastLogin(userId: Uuid) {
        val user = users[userId] ?: return
        val now = Clock.System.now()
        users[userId] = user.copy(letzteAnmeldung = now, updatedAt = now)
    }

    override suspend fun incrementFailedLoginAttempts(userId: Uuid) {
        val user = users[userId] ?: return
        val now = Clock.System.now()
        users[userId] = user.copy(
            fehlgeschlageneAnmeldungen = user.fehlgeschlageneAnmeldungen + 1,
            updatedAt = now
        )
    }

    override suspend fun resetFailedLoginAttempts(userId: Uuid) {
        val user = users[userId] ?: return
        val now = Clock.System.now()
        users[userId] = user.copy(fehlgeschlageneAnmeldungen = 0, updatedAt = now)
    }

    override suspend fun lockUser(userId: Uuid, lockedUntil: Instant) {
        val user = users[userId] ?: return
        val now = Clock.System.now()
        users[userId] = user.copy(gesperrtBis = lockedUntil, updatedAt = now)
    }

    override suspend fun unlockUser(userId: Uuid) {
        val user = users[userId] ?: return
        val now = Clock.System.now()
        users[userId] = user.copy(gesperrtBis = null, updatedAt = now)
    }

    override suspend fun setUserActive(userId: Uuid, isActive: Boolean) {
        val user = users[userId] ?: return
        val now = Clock.System.now()
        users[userId] = user.copy(istAktiv = isActive, updatedAt = now)
    }

    override suspend fun markEmailAsVerified(userId: Uuid) {
        val user = users[userId] ?: return
        val now = Clock.System.now()
        users[userId] = user.copy(istEmailVerifiziert = true, updatedAt = now)
    }

    override suspend fun updatePassword(userId: Uuid, passwordHash: String, salt: String) {
        val user = users[userId] ?: return
        val now = Clock.System.now()
        users[userId] = user.copy(passwordHash = passwordHash, salt = salt, updatedAt = now)
    }

    override suspend fun deleteUser(userId: Uuid): Boolean {
        return users.remove(userId) != null
    }

    override suspend fun getAllUsers(): List<DomUser> {
        return users.values.toList()
    }

    override suspend fun getActiveUsers(): List<DomUser> {
        return users.values.filter { it.istAktiv }
    }
}
