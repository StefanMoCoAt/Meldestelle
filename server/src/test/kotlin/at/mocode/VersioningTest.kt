package at.mocode

import at.mocode.dto.ArtikelDto
import at.mocode.dto.VereinDto
import at.mocode.dto.base.VersionManager
import at.mocode.dto.base.VersionValidationResult
import at.mocode.dto.migrations.ArtikelDtoMigrator
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class VersioningTest {

    @Test
    fun testVersionManagerValidation() {
        // Test current version (1.1)
        val validResult = VersionManager.validateClientVersion("1.1")
        assertIs<VersionValidationResult.Valid>(validResult)
        assertEquals("1.1", validResult.version)

        // Test the deprecated version (1.0)
        val deprecatedResult = VersionManager.validateClientVersion("1.0")
        assertIs<VersionValidationResult.DeprecatedVersion>(deprecatedResult)
        assertEquals("1.0", deprecatedResult.version)

        // Test unsupported version
        val unsupportedResult = VersionManager.validateClientVersion("2.0")
        assertIs<VersionValidationResult.UnsupportedVersion>(unsupportedResult)
        assertEquals("2.0", unsupportedResult.version)

        // Test missing version
        val missingResult = VersionManager.validateClientVersion(null)
        assertIs<VersionValidationResult.MissingVersion>(missingResult)
    }

    @Test
    fun testVersionManagerInfo() {
        val versionInfo = VersionManager.getVersionInfo()
        assertEquals("1.1", versionInfo.apiVersion)
        assertTrue(versionInfo.supportedVersions.contains("1.1"))
        assertTrue(versionInfo.supportedVersions.contains("1.0"))
        assertEquals("1.0", versionInfo.minimumClientVersion)
    }

    @Test
    fun testArtikelDtoVersioning() {
        val artikel = ArtikelDto(
            id = uuid4(),
            bezeichnung = "Test Artikel",
            preis = BigDecimal.fromInt(100),
            einheit = "Stück",
            istVerbandsabgabe = false,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            schemaVersion = "1.0",
            dataVersion = 1L
        )

        assertEquals("1.0", artikel.schemaVersion)
        assertEquals(1L, artikel.dataVersion)
    }

    @Test
    fun testVereinDtoVersioning() {
        val verein = VereinDto(
            id = uuid4(),
            oepsVereinsNr = "12345",
            name = "Test Verein",
            kuerzel = "TV",
            bundesland = "Wien",
            adresse = "Teststraße 1",
            plz = "1010",
            ort = "Wien",
            email = "test@verein.at",
            telefon = "+43123456789",
            webseite = "www.testverein.at",
            istAktiv = true,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            schemaVersion = "1.0",
            dataVersion = 1L
        )

        assertEquals("1.0", verein.schemaVersion)
        assertEquals(1L, verein.dataVersion)
    }

    @Test
    fun testArtikelDtoMigrator() {
        val migrator = ArtikelDtoMigrator()

        // Test migration capability
        assertTrue(migrator.canMigrate("1.0", "1.0"))

        val artikel = ArtikelDto(
            id = uuid4(),
            bezeichnung = "Test Artikel",
            preis = BigDecimal.fromInt(100),
            einheit = "Stück",
            istVerbandsabgabe = false,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            schemaVersion = "1.0",
            dataVersion = 1L
        )

        // Test migration (same version should return same object)
        val migratedArtikel = migrator.migrate(artikel, "1.0", "1.0")
        assertEquals(artikel, migratedArtikel)
    }

    @Test
    fun testVersionSupport() {
        assertTrue(VersionManager.isVersionSupported("1.0"))
        assertTrue(VersionManager.isVersionSupported("1.1"))
        assertTrue(!VersionManager.isVersionSupported("2.0"))
        assertTrue(VersionManager.isVersionDeprecated("1.0"))
        assertTrue(!VersionManager.isVersionDeprecated("1.1"))
    }
}
