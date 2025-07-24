package at.mocode.members.infrastructure.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp

/**
 * Database table definition for members in the member management context.
 *
 * This table stores member information including personal details,
 * membership information, and contact details.
 */
object MemberTable : Table("members") {
    val id = uuid("id").autoGenerate()
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val email = varchar("email", 255).uniqueIndex()
    val phone = varchar("phone", 50).nullable()
    val dateOfBirth = date("date_of_birth").nullable()
    val membershipNumber = varchar("membership_number", 50).uniqueIndex()
    val membershipStartDate = date("membership_start_date")
    val membershipEndDate = date("membership_end_date").nullable()
    val isActive = bool("is_active").default(true)
    val address = varchar("address", 500).nullable()
    val emergencyContact = varchar("emergency_contact", 255).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}
