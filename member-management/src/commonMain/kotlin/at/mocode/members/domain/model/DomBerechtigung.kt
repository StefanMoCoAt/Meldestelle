package at.mocode.members.domain.model

import at.mocode.enums.BerechtigungE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert eine Berechtigung im System für die Zugriffskontrolle.
 *
 * Berechtigungen definieren spezifische Aktionen, die im System ausgeführt werden können
 * (z.B. Personen lesen, Vereine erstellen, Veranstaltungen bearbeiten).
 * Berechtigungen werden Rollen zugeordnet, die wiederum Personen zugewiesen werden.
 *
 * @property berechtigungId Eindeutiger interner Identifikator für diese Berechtigung (UUID).
 * @property berechtigungTyp Der Typ der Berechtigung aus der BerechtigungE Enumeration.
 * @property name Anzeigename der Berechtigung (z.B. "Personen lesen", "Vereine erstellen").
 * @property beschreibung Detaillierte Beschreibung der Berechtigung und ihres Zwecks.
 * @property ressource Die Ressource, auf die sich diese Berechtigung bezieht (z.B. "Person", "Verein").
 * @property aktion Die Aktion, die mit dieser Berechtigung ausgeführt werden kann (z.B. "lesen", "erstellen").
 * @property istAktiv Gibt an, ob diese Berechtigung aktuell aktiv ist.
 * @property istSystemBerechtigung Gibt an, ob es sich um eine Systemberechtigung handelt, die nicht gelöscht werden kann.
 * @property createdAt Zeitstempel der Erstellung dieser Berechtigung.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieser Berechtigung.
 */
@Serializable
data class DomBerechtigung(
    @Serializable(with = UuidSerializer::class)
    val berechtigungId: Uuid = uuid4(),

    val berechtigungTyp: BerechtigungE,
    var name: String,
    var beschreibung: String? = null,
    var ressource: String,
    var aktion: String,

    var istAktiv: Boolean = true,
    var istSystemBerechtigung: Boolean = false,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
