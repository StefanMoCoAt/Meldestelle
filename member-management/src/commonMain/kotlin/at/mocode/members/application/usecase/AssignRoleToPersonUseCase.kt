package at.mocode.members.application.usecase

import at.mocode.members.domain.model.DomPersonRolle
import at.mocode.members.domain.repository.PersonRepository
import at.mocode.members.domain.repository.PersonRolleRepository
import at.mocode.members.domain.repository.RolleRepository
import at.mocode.members.domain.repository.VereinRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

/**
 * Use Case für das Zuweisen einer Rolle zu einer Person.
 *
 * Dieser Use Case validiert die Eingabedaten und erstellt eine neue Person-Rolle-Zuordnung,
 * falls diese noch nicht existiert.
 */
class AssignRoleToPersonUseCase(
    private val personRepository: PersonRepository,
    private val rolleRepository: RolleRepository,
    private val personRolleRepository: PersonRolleRepository,
    private val vereinRepository: VereinRepository
) {

    /**
     * Weist einer Person eine Rolle zu.
     *
     * @param request Die Anfrage mit den Zuordnungsdaten.
     * @return Die erstellte Person-Rolle-Zuordnung.
     * @throws IllegalArgumentException wenn ungültige Daten übergeben wurden oder die Zuordnung bereits existiert.
     */
    suspend fun execute(request: AssignRoleToPersonRequest): DomPersonRolle {
        // Validierung der Eingabedaten
        validateRequest(request)

        // Prüfen, ob Person existiert
        val person = personRepository.findById(request.personId)
            ?: throw IllegalArgumentException("Person mit ID '${request.personId}' wurde nicht gefunden.")

        // Prüfen, ob Rolle existiert
        val rolle = rolleRepository.findById(request.rolleId)
            ?: throw IllegalArgumentException("Rolle mit ID '${request.rolleId}' wurde nicht gefunden.")

        // Prüfen, ob Rolle aktiv ist
        if (!rolle.istAktiv) {
            throw IllegalArgumentException("Die Rolle '${rolle.name}' ist nicht aktiv und kann nicht zugewiesen werden.")
        }

        // Prüfen, ob Verein existiert (falls angegeben)
        request.vereinId?.let { vereinId ->
            val verein = vereinRepository.findById(vereinId)
                ?: throw IllegalArgumentException("Verein mit ID '$vereinId' wurde nicht gefunden.")

            if (!verein.istAktiv) {
                throw IllegalArgumentException("Der Verein '${verein.name}' ist nicht aktiv.")
            }
        }

        // Prüfen, ob die Zuordnung bereits existiert
        val existierendeZuordnung = personRolleRepository.findByPersonAndRolle(
            request.personId,
            request.rolleId,
            request.vereinId
        )

        if (existierendeZuordnung != null && existierendeZuordnung.istAktiv) {
            throw IllegalArgumentException("Die Person '${person.nachname}, ${person.vorname}' hat bereits die Rolle '${rolle.name}'.")
        }

        // Neue Person-Rolle-Zuordnung erstellen
        val personRolle = DomPersonRolle(
            personId = request.personId,
            rolleId = request.rolleId,
            vereinId = request.vereinId,
            gueltigVon = request.gueltigVon,
            gueltigBis = request.gueltigBis,
            istAktiv = true,
            zugewiesenVon = request.zugewiesenVon,
            notizen = request.notizen,
            updatedAt = Clock.System.now()
        )

        // Person-Rolle-Zuordnung speichern
        return personRolleRepository.save(personRolle)
    }

    private fun validateRequest(request: AssignRoleToPersonRequest) {
        // Prüfen, ob gueltigBis nach gueltigVon liegt
        request.gueltigBis?.let { gueltigBis ->
            if (gueltigBis <= request.gueltigVon) {
                throw IllegalArgumentException("Das Enddatum muss nach dem Startdatum liegen.")
            }
        }

        // Prüfen, ob gueltigVon nicht in der Vergangenheit liegt (optional, je nach Geschäftslogik)
        // Hier könnte man auch erlauben, dass Rollen rückwirkend zugewiesen werden

        request.notizen?.let { notizen ->
            if (notizen.length > 1000) {
                throw IllegalArgumentException("Die Notizen dürfen maximal 1000 Zeichen lang sein.")
            }
        }
    }
}

/**
 * Request-Datenklasse für das Zuweisen einer Rolle zu einer Person.
 */
data class AssignRoleToPersonRequest(
    val personId: Uuid,
    val rolleId: Uuid,
    val vereinId: Uuid? = null,
    val gueltigVon: LocalDate,
    val gueltigBis: LocalDate? = null,
    val zugewiesenVon: Uuid? = null,
    val notizen: String? = null
)
