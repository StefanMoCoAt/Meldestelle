package at.mocode.members.domain.model

import at.mocode.enums.RolleE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert eine Rolle im System für die Mitgliederverwaltung.
 *
 * Rollen definieren die grundlegenden Funktionen und Verantwortlichkeiten
 * von Personen im System (z.B. Reiter, Trainer, Funktionär, Admin).
 * Jede Rolle kann mit spezifischen Berechtigungen verknüpft werden.
 *
 * @property rolleId Eindeutiger interner Identifikator für diese Rolle (UUID).
 * @property rolleTyp Der Typ der Rolle aus der RolleE Enumeration.
 * @property name Anzeigename der Rolle (z.B. "Administrator", "Vereinsadministrator").
 * @property beschreibung Detaillierte Beschreibung der Rolle und ihrer Verantwortlichkeiten.
 * @property istAktiv Gibt an, ob diese Rolle aktuell aktiv ist und zugewiesen werden kann.
 * @property istSystemRolle Gibt an, ob es sich um eine Systemrolle handelt, die nicht gelöscht werden kann.
 * @property createdAt Zeitstempel der Erstellung dieser Rolle.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieser Rolle.
 */
@Serializable
data class DomRolle(
    @Serializable(with = UuidSerializer::class)
    val rolleId: Uuid = uuid4(),

    val rolleTyp: RolleE,
    var name: String,
    var beschreibung: String? = null,

    var istAktiv: Boolean = true,
    var istSystemRolle: Boolean = false,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
