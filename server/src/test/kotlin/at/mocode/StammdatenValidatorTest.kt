package at.mocode

import at.mocode.model.domaene.*
import at.mocode.validation.*
import at.mocode.enums.DatenQuelleE
import at.mocode.enums.PferdeGeschlechtE
import com.benasher44.uuid.uuid4
import kotlinx.datetime.LocalDate
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StammdatenValidatorTest {

    @Test
    fun testDomVereinValidator() {
        // Valid club
        val validClub = DomVerein(
            oepsVereinsNr = "1234",
            name = "Test Reitverein",
            kuerzel = "TRV",
            landId = uuid4(),
            emailAllgemein = "test@example.com"
        )

        val result = DomVereinValidator.validate(validClub)
        if (result.isInvalid()) {
            println("[DEBUG_LOG] DomVerein validation errors: ${(result as ValidationResult.Invalid).errors}")
        }
        assertTrue(DomVereinValidator.isValid(validClub))

        // Invalid club - empty name
        val invalidClub = DomVerein(
            oepsVereinsNr = "1234",
            name = "",
            landId = uuid4()
        )

        assertFalse(DomVereinValidator.isValid(invalidClub))
    }

    @Test
    fun testDomPferdValidator() {
        // Valid horse
        val validHorse = DomPferd(
            name = "Test Pferd",
            oepsSatzNrPferd = "1234567890",
            oepsKopfNr = "1234",
            geburtsjahr = 2015,
            geschlecht = PferdeGeschlechtE.STUTE
        )

        assertTrue(DomPferdValidator.isValid(validHorse))

        // Invalid horse - empty name
        val invalidHorse = DomPferd(
            name = "",
            oepsSatzNrPferd = "1234567890",
            oepsKopfNr = "1234"
        )

        assertFalse(DomPferdValidator.isValid(invalidHorse))
    }

    @Test
    fun testDomLizenzValidator() {
        // Valid license
        val validLicense = DomLizenz(
            personId = uuid4(),
            lizenzTypGlobalId = uuid4(),
            gueltigBisJahr = 2024,
            istAktivBezahltOeps = true
        )

        assertTrue(DomLizenzValidator.isValid(validLicense))

        // Test expiry check
        val expiredLicense = DomLizenz(
            personId = uuid4(),
            lizenzTypGlobalId = uuid4(),
            gueltigBisJahr = 2020,
            istAktivBezahltOeps = true
        )

        assertTrue(DomLizenzValidator.isLicenseExpired(expiredLicense))
    }

    @Test
    fun testDomQualifikationValidator() {
        // Valid qualification
        val validQualification = DomQualifikation(
            personId = uuid4(),
            qualTypId = uuid4(),
            gueltigVon = LocalDate(2020, 1, 1),
            gueltigBis = LocalDate(2025, 12, 31),
            istAktiv = true
        )

        val qualResult = DomQualifikationValidator.validate(validQualification)
        if (qualResult.isInvalid()) {
            println("[DEBUG_LOG] DomQualifikation validation errors: ${(qualResult as ValidationResult.Invalid).errors}")
        }
        assertTrue(DomQualifikationValidator.isValid(validQualification))
        assertTrue(DomQualifikationValidator.isCurrentlyValid(validQualification))

        // Invalid qualification - end before start
        val invalidQualification = DomQualifikation(
            personId = uuid4(),
            qualTypId = uuid4(),
            gueltigVon = LocalDate(2025, 1, 1),
            gueltigBis = LocalDate(2020, 12, 31),
            istAktiv = true
        )

        assertFalse(DomQualifikationValidator.isValid(invalidQualification))
    }
}
