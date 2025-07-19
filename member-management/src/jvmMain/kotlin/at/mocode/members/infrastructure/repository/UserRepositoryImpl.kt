package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.repository.UserRepository
import at.mocode.members.domain.model.DomUser
import at.mocode.shared.database.DatabaseFactory
import at.mocode.members.infrastructure.table.UserTable
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.statements.InsertStatement

/**
 * Implementation des UserRepository für die Datenbankzugriffe.
 */
class UserRepositoryImpl : UserRepository {

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt.
     */
    private fun rowToDomUser(row: ResultRow): DomUser {
        return DomUser(
            userId = row[UserTable.id],
            personId = row[UserTable.personId],
            username = row[UserTable.username],
            email = row[UserTable.email],
            passwordHash = row[UserTable.passwordHash],
            salt = row[UserTable.salt],
            istAktiv = row[UserTable.isActive],
            istEmailVerifiziert = row[UserTable.isEmailVerified],
            fehlgeschlageneAnmeldungen = row[UserTable.failedLoginAttempts],
            gesperrtBis = row[UserTable.lockedUntil],
            letzteAnmeldung = row[UserTable.lastLoginAt],
            createdAt = row[UserTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[UserTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }

    override suspend fun createUser(user: DomUser): DomUser = DatabaseFactory.dbQuery {
        val stmt = UserTable.insert { insertStmt ->
            populateUserStatement(insertStmt, user)
        }

        val userId = stmt[UserTable.id]
        findById(userId)!!
    }

    private fun populateUserStatement(stmt: InsertStatement<*>, user: DomUser) {
        stmt[UserTable.id] = user.userId
        stmt[UserTable.personId] = user.personId
        stmt[UserTable.username] = user.username
        stmt[UserTable.email] = user.email
        stmt[UserTable.passwordHash] = user.passwordHash
        stmt[UserTable.salt] = user.salt
        stmt[UserTable.isActive] = user.istAktiv
        stmt[UserTable.isEmailVerified] = user.istEmailVerifiziert
        stmt[UserTable.failedLoginAttempts] = user.fehlgeschlageneAnmeldungen
        stmt[UserTable.lockedUntil] = user.gesperrtBis
        stmt[UserTable.lastLoginAt] = user.letzteAnmeldung
        stmt[UserTable.createdAt] = user.createdAt.toLocalDateTime(TimeZone.UTC)
        stmt[UserTable.updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    }

    override suspend fun findById(userId: Uuid): DomUser? = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.id eq userId }
            .map(::rowToDomUser)
            .singleOrNull()
    }

    override suspend fun findByUsername(username: String): DomUser? = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.username eq username }
            .map(::rowToDomUser)
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): DomUser? = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.email eq email }
            .map(::rowToDomUser)
            .singleOrNull()
    }

    override suspend fun findByPersonId(personId: Uuid): DomUser? = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.personId eq personId }
            .map(::rowToDomUser)
            .singleOrNull()
    }

    override suspend fun updateUser(user: DomUser): DomUser = DatabaseFactory.dbQuery {
        val updatedUser = user.copy(updatedAt = Clock.System.now())

        UserTable.update({ UserTable.id eq user.userId }) { updateStmt ->
            updateStmt[UserTable.username] = updatedUser.username
            updateStmt[UserTable.email] = updatedUser.email
            updateStmt[UserTable.passwordHash] = updatedUser.passwordHash
            updateStmt[UserTable.salt] = updatedUser.salt
            updateStmt[UserTable.isActive] = updatedUser.istAktiv
            updateStmt[UserTable.isEmailVerified] = updatedUser.istEmailVerifiziert
            updateStmt[UserTable.failedLoginAttempts] = updatedUser.fehlgeschlageneAnmeldungen
            updateStmt[UserTable.lockedUntil] = updatedUser.gesperrtBis
            updateStmt[UserTable.lastLoginAt] = updatedUser.letzteAnmeldung
            updateStmt[UserTable.updatedAt] = updatedUser.updatedAt.toLocalDateTime(TimeZone.UTC)
        }

        findById(user.userId)!!
    }

    override suspend fun updateLastLogin(userId: Uuid) = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        UserTable.update({ UserTable.id eq userId }) { updateStmt ->
            updateStmt[UserTable.lastLoginAt] = now
            updateStmt[UserTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }
        Unit
    }

    override suspend fun incrementFailedLoginAttempts(userId: Uuid) = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        UserTable.update({ UserTable.id eq userId }) { updateStmt ->
            updateStmt[UserTable.failedLoginAttempts] = UserTable.failedLoginAttempts + 1
            updateStmt[UserTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }
        Unit
    }

    override suspend fun resetFailedLoginAttempts(userId: Uuid) = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        UserTable.update({ UserTable.id eq userId }) { updateStmt ->
            updateStmt[UserTable.failedLoginAttempts] = 0
            updateStmt[UserTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }
        Unit
    }

    override suspend fun lockUser(userId: Uuid, lockedUntil: Instant) = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        UserTable.update({ UserTable.id eq userId }) { updateStmt ->
            updateStmt[UserTable.lockedUntil] = lockedUntil
            updateStmt[UserTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }
        Unit
    }

    override suspend fun unlockUser(userId: Uuid) = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        UserTable.update({ UserTable.id eq userId }) { updateStmt ->
            updateStmt[UserTable.lockedUntil] = null
            updateStmt[UserTable.failedLoginAttempts] = 0
            updateStmt[UserTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }
        Unit
    }

    override suspend fun setUserActive(userId: Uuid, isActive: Boolean) = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        UserTable.update({ UserTable.id eq userId }) { updateStmt ->
            updateStmt[UserTable.isActive] = isActive
            updateStmt[UserTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }
        Unit
    }

    override suspend fun markEmailAsVerified(userId: Uuid) = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        UserTable.update({ UserTable.id eq userId }) { updateStmt ->
            updateStmt[UserTable.isEmailVerified] = true
            updateStmt[UserTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }
        Unit
    }

    override suspend fun updatePassword(userId: Uuid, passwordHash: String, salt: String) = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        UserTable.update({ UserTable.id eq userId }) { updateStmt ->
            updateStmt[UserTable.passwordHash] = passwordHash
            updateStmt[UserTable.salt] = salt
            updateStmt[UserTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }
        Unit
    }

    override suspend fun deleteUser(userId: Uuid): Boolean = DatabaseFactory.dbQuery {
        UserTable.deleteWhere { UserTable.id eq userId } > 0
    }

    override suspend fun getAllUsers(): List<DomUser> = DatabaseFactory.dbQuery {
        UserTable.selectAll()
            .map(::rowToDomUser)
    }

    override suspend fun getActiveUsers(): List<DomUser> = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.isActive eq true }
            .map(::rowToDomUser)
    }
}
