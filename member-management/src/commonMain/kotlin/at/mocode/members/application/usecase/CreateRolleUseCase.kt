package at.mocode.members.application.usecase

import at.mocode.enums.RolleE
import at.mocode.members.domain.model.DomRolle
import at.mocode.members.domain.repository.RolleRepository
import kotlinx.datetime.Clock

/**
 * Use Case für das Erstellen einer neuen Rolle im System.
 *
 * Dieser Use Case validiert die Eingabedaten und erstellt eine neue Rolle,
 * falls diese noch nicht existiert.
 */
class CreateRolleUseCase(
    private val rolleRepository: RolleRepository
) {

    /**
     * Erstellt eine neue Rolle im System.
     *
     * @param request Die Anfrage mit den Rollendaten.
     * @return Die erstellte Rolle.
     * @throws IllegalArgumentException wenn die Rolle bereits existiert oder ungültige Daten übergeben wurden.
     */
    suspend fun execute(request: CreateRolleRequest): DomRolle {
        // Validierung der Eingabedaten
        validateRequest(request)

        // Prüfen, ob eine Rolle mit diesem Typ bereits existiert
        if (rolleRepository.existsByTyp(request.rolleTyp)) {
            throw IllegalArgumentException("Eine Rolle mit dem Typ '${request.rolleTyp}' existiert bereits.")
        }

        // Neue Rolle erstellen
        val neueRolle = DomRolle(
            rolleTyp = request.rolleTyp,
            name = request.name,
            beschreibung = request.beschreibung,
            istAktiv = request.istAktiv ?: true,
            istSystemRolle = request.istSystemRolle ?: false,
            updatedAt = Clock.System.now()
        )

        // Rolle speichern
        return rolleRepository.save(neueRolle)
    }

    private fun validateRequest(request: CreateRolleRequest) {
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Der Name der Rolle darf nicht leer sein.")
        }

        if (request.name.length > 100) {
            throw IllegalArgumentException("Der Name der Rolle darf maximal 100 Zeichen lang sein.")
        }

        request.beschreibung?.let { beschreibung ->
            if (beschreibung.length > 500) {
                throw IllegalArgumentException("Die Beschreibung der Rolle darf maximal 500 Zeichen lang sein.")
            }
        }
    }
}

/**
 * Request-Datenklasse für das Erstellen einer Rolle.
 */
data class CreateRolleRequest(
    val rolleTyp: RolleE,
    val name: String,
    val beschreibung: String? = null,
    val istAktiv: Boolean? = null,
    val istSystemRolle: Boolean? = null
)
