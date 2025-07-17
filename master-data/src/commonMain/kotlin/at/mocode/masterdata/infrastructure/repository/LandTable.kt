package at.mocode.masterdata.infrastructure.repository

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Database table definition for LandDefinition (Country) entities.
 *
 * This table stores country reference data including ISO codes,
 * names in multiple languages, and EU/EWR membership information.
 */
object LandTable : UUIDTable("land_definition") {

    // ISO Codes
    val isoAlpha2Code = varchar("iso_alpha2_code", 2).uniqueIndex()
    val isoAlpha3Code = varchar("iso_alpha3_code", 3).uniqueIndex()
    val isoNumericCode = varchar("iso_numeric_code", 3).nullable()

    // Names
    val nameGerman = varchar("name_german", 100)
    val nameEnglish = varchar("name_english", 100)
    val nameLocal = varchar("name_local", 100).nullable()

    // Status and Membership
    val isActive = bool("is_active").default(true)
    val isEuMember = bool("is_eu_member").default(false)
    val isEwrMember = bool("is_ewr_member").default(false)

    // Sorting and Display
    val sortierReihenfolge = integer("sortier_reihenfolge").default(999)
    val flagIcon = varchar("flag_icon", 10).nullable()

    // Audit fields
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val createdBy = varchar("created_by", 50).nullable()
    val updatedBy = varchar("updated_by", 50).nullable()

    // Additional metadata
    val notes = text("notes").nullable()
}
