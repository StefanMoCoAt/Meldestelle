package at.mocode.members.domain.model

import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert die Zuordnung einer Berechtigung zu einer Rolle.
 *
 * Diese Entität verwaltet die Many-to-Many-Beziehung zwischen Rollen und Berechtigungen.
 * Eine Rolle kann mehrere Berechtigungen haben (z.B. Trainer kann Personen lesen und Pferde bearbeiten),
 * und eine Berechtigung kann mehreren Rollen zugeordnet werden.
 *
 * @property rolleBerechtigungId Eindeutiger interner Identifikator für diese Berechtigungszuordnung (UUID).
 * @property rolleId Fremdschlüssel zur Rolle (DomRolle.rolleId).
 * @property berechtigungId Fremdschlüssel zur Berechtigung (DomBerechtigung.berechtigungId).
 * @property istAktiv Gibt an, ob diese Berechtigungszuordnung aktuell aktiv ist.
 * @property zugewiesenVon Optionale Referenz auf die Person, die diese Berechtigung zugewiesen hat.
 * @property notizen Optionale Notizen zur Berechtigungszuordnung.
 * @property createdAt Zeitstempel der Erstellung dieser Berechtigungszuordnung.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieser Berechtigungszuordnung.
 */
@Serializable
data class DomRolleBerechtigung(
    @Serializable(with = UuidSerializer::class)
    val rolleBerechtigungId: Uuid = uuid4(),

    @Serializable(with = UuidSerializer::class)
    val rolleId: Uuid,

    @Serializable(with = UuidSerializer::class)
    val berechtigungId: Uuid,

    var istAktiv: Boolean = true,

    @Serializable(with = UuidSerializer::class)
    var zugewiesenVon: Uuid? = null, // PersonId des Zuweisers

    var notizen: String? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
