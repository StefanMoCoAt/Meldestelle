package at.mocode

import at.mocode.model.domaene.DomQualifikation
import com.benasher44.uuid.uuid4
import kotlinx.datetime.LocalDate
import kotlin.test.*

class DomQualifikationTest {

    @Test
    fun testDomQualifikationCreation() {
        val personId = uuid4()
        val qualTypId = uuid4()

        val qualification = DomQualifikation(
            personId = personId,
            qualTypId = qualTypId,
            bemerkung = "Test qualification",
            gueltigVon = LocalDate(2024, 1, 1),
            gueltigBis = LocalDate(2024, 12, 31),
            istAktiv = true
        )

        assertEquals(personId, qualification.personId)
        assertEquals(qualTypId, qualification.qualTypId)
        assertEquals("Test qualification", qualification.bemerkung)
        assertEquals(LocalDate(2024, 1, 1), qualification.gueltigVon)
        assertEquals(LocalDate(2024, 12, 31), qualification.gueltigBis)
        assertTrue(qualification.istAktiv)
        assertNotNull(qualification.qualifikationId)
        assertNotNull(qualification.createdAt)
        assertNotNull(qualification.updatedAt)
    }

    @Test
    fun testDomQualifikationDefaults() {
        val personId = uuid4()
        val qualTypId = uuid4()

        val qualification = DomQualifikation(
            personId = personId,
            qualTypId = qualTypId
        )

        assertEquals(personId, qualification.personId)
        assertEquals(qualTypId, qualification.qualTypId)
        assertNull(qualification.bemerkung)
        assertNull(qualification.gueltigVon)
        assertNull(qualification.gueltigBis)
        assertTrue(qualification.istAktiv) // Default should be true
        assertNotNull(qualification.qualifikationId)
        assertNotNull(qualification.createdAt)
        assertNotNull(qualification.updatedAt)
    }
}
