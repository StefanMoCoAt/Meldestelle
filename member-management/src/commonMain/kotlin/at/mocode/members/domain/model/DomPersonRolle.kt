package at.mocode.members.domain.model

import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.KotlinLocalDateSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Repräsentiert die Zuordnung einer Rolle zu einer Person.
 *
 * Diese Entität verwaltet die Many-to-Many-Beziehung zwischen Personen und Rollen.
 * Eine Person kann mehrere Rollen haben (z.B. gleichzeitig Reiter und Trainer),
 * und eine Rolle kann mehreren Personen zugeordnet werden.
 *
 * @property personRolleId Eindeutiger interner Identifikator für diese Rollenzuordnung (UUID).
 * @property personId Fremdschlüssel zur Person (DomPerson.personId).
 * @property rolleId Fremdschlüssel zur Rolle (DomRolle.rolleId).
 * @property vereinId Optionale Verknüpfung zu einem Verein, falls die Rolle vereinsspezifisch ist.
 * @property gueltigVon Datum, ab dem diese Rollenzuordnung gültig ist.
 * @property gueltigBis Optionales Datum, bis zu dem diese Rollenzuordnung gültig ist.
 * @property istAktiv Gibt an, ob diese Rollenzuordnung aktuell aktiv ist.
 * @property zugewiesenVon Optionale Referenz auf die Person, die diese Rolle zugewiesen hat.
 * @property notizen Optionale Notizen zur Rollenzuordnung.
 * @property createdAt Zeitstempel der Erstellung dieser Rollenzuordnung.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieser Rollenzuordnung.
 */
@Serializable
data class DomPersonRolle(
    @Serializable(with = UuidSerializer::class)
    val personRolleId: Uuid = uuid4(),

    @Serializable(with = UuidSerializer::class)
    val personId: Uuid,

    @Serializable(with = UuidSerializer::class)
    val rolleId: Uuid,

    @Serializable(with = UuidSerializer::class)
    var vereinId: Uuid? = null, // Für vereinsspezifische Rollen

    @Serializable(with = KotlinLocalDateSerializer::class)
    var gueltigVon: LocalDate,

    @Serializable(with = KotlinLocalDateSerializer::class)
    var gueltigBis: LocalDate? = null,

    var istAktiv: Boolean = true,

    @Serializable(with = UuidSerializer::class)
    var zugewiesenVon: Uuid? = null, // PersonId des Zuweisers

    var notizen: String? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
