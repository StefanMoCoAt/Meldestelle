package at.mocode.masterdata.domain.model

import at.mocode.core.domain.model.PlatzTypE
import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Definiert einen Turnierplatz oder eine Wettkampfstätte.
 *
 * Diese Entität repräsentiert die verschiedenen Plätze und Arenen, die bei Turnieren
 * für verschiedene Disziplinen verwendet werden können.
 *
 * @property id Eindeutiger interner Identifikator für diesen Platz (UUID).
 * @property turnierId Fremdschlüssel zum Turnier, zu dem dieser Platz gehört.
 * @property name Der Name oder die Bezeichnung des Platzes (z.B. "Hauptplatz", "Dressurplatz A").
 * @property dimension Die Abmessungen des Platzes (z.B. "20x60m", "20x40m").
 * @property boden Die Art des Bodenbelags (z.B. "Sand", "Gras", "Kunststoff").
 * @property typ Der Typ des Platzes (siehe PlatzTypE enum).
 * @property istAktiv Gibt an, ob dieser Platz aktuell verwendet werden kann.
 * @property sortierReihenfolge Optionale Zahl zur Steuerung der Sortierreihenfolge.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class Platz(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),

    @Serializable(with = UuidSerializer::class)
    var turnierId: Uuid,

    var name: String,
    var dimension: String? = null,
    var boden: String? = null,
    var typ: PlatzTypE,
    var istAktiv: Boolean = true,
    var sortierReihenfolge: Int? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
