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
 * Repräsentiert eine Rolle im System für die Zugriffskontrolle.
 *
 * Rollen bündeln mehrere Berechtigungen und werden Personen zugewiesen,
 * um deren Zugriffsrechte im System zu definieren.
 *
 * @property rolleId Eindeutiger interner Identifikator für diese Rolle (UUID).
 * @property rolleTyp Der Typ der Rolle (Enum-Wert).
 * @property name Anzeigename der Rolle (z.B. "Administrator", "Vereinsverwalter").
 * @property beschreibung Detaillierte Beschreibung der Rolle und ihres Zwecks.
 * @property istSystemRolle Gibt an, ob es sich um eine Systemrolle handelt, die nicht gelöscht werden kann.
 * @property istAktiv Gibt an, ob diese Rolle aktuell aktiv ist.
 * @property createdAt Zeitstempel der Erstellung dieser Rolle.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieser Rolle.
 */
@Serializable
data class DomRolle(
    @Serializable(with = UuidSerializer::class)
    val rolleId: Uuid = uuid4(),

    var rolleTyp: RolleE,
    var name: String,
    var beschreibung: String? = null,

    var istSystemRolle: Boolean = false,
    var istAktiv: Boolean = true,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
