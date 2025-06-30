package at.mocode.dto.migrations

import at.mocode.dto.ArtikelDto
import at.mocode.dto.base.VersionMigrator

/**
 * Migrator for ArtikelDto versions.
 * Handles migration between different versions of ArtikelDto.
 */
class ArtikelDtoMigrator : VersionMigrator<ArtikelDto> {

    override fun migrate(dto: ArtikelDto, fromVersion: String, toVersion: String): ArtikelDto {
        return when {
            fromVersion == "1.0" && toVersion == "1.0" -> dto
            // Future migrations would be handled here
            // fromVersion == "1.0" && toVersion == "1.1" -> migrateFrom1_0To1_1(dto)
            // fromVersion == "1.1" && toVersion == "1.2" -> migrateFrom1_1To1_2(dto)
            else -> throw IllegalArgumentException("Unsupported migration from $fromVersion to $toVersion")
        }
    }

    override fun canMigrate(fromVersion: String, toVersion: String): Boolean {
        return when {
            fromVersion == "1.0" && toVersion == "1.0" -> true
            // Future migration paths would be defined here
            // fromVersion == "1.0" && toVersion == "1.1" -> true
            // fromVersion == "1.1" && toVersion == "1.2" -> true
            else -> false
        }
    }

    // Example of future migration method
    // private fun migrateFrom1_0To1_1(dto: ArtikelDto): ArtikelDto {
    //     return dto.copy(
    //         schemaVersion = "1.1",
    //         // Add new fields with default values
    //         // newField = "defaultValue"
    //     )
    // }
}
