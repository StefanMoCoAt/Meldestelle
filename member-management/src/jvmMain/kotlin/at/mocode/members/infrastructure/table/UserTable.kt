package at.mocode.members.infrastructure.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime

/**
 * Exposed-Tabellendefinition für die User-Entität.
 */
object UserTable : Table("benutzer") {
    val id = uuid("id").autoGenerate()
    val personId = uuid("person_id")
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val salt = varchar("salt", 64)
    val isActive = bool("is_active").default(true)
    val isEmailVerified = bool("is_email_verified").default(false)
    val failedLoginAttempts = integer("failed_login_attempts").default(0)
    val lockedUntil = timestamp("locked_until").nullable()
    val lastLoginAt = timestamp("last_login_at").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
