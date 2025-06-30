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
 * Repräsentiert eine spezifische Funktionärsqualifikation, die einer Person (`DomPerson`)
 * zugeordnet ist.
 *
 * Diese Entität verknüpft eine Person mit einer globalen Qualifikationsdefinition (`QualifikationsTyp`)
 * und kann zusätzliche Informationen wie eine spezifische Bemerkung oder Gültigkeit speichern.
 * Die Informationen stammen primär aus der Verarbeitung der Roh-Qualifikationskürzel
 * aus `Person_ZNS_Staging.qualifikationenRawOepsRoh` (welches aus `RICHT01.dat` befüllt wird).
 *
 * @property qualifikationId Eindeutiger interner Identifikator für diese zugeordnete Qualifikation (UUID).
 * @property personId Fremdschlüssel zur `DomPerson`, die diese Qualifikation besitzt.
 * @property qualTypId Fremdschlüssel zum `QualifikationsTyp`, der die Art dieser Qualifikation definiert.
 * @property bemerkung Optionale Bemerkungen zu dieser spezifischen Qualifikation der Person
 * (z.B. spezifische Einsatzbereiche, Einschränkungen).
 * @property gueltigVon Optionales Datum, ab wann diese Qualifikation für die Person gültig ist.
 * @property gueltigBis Optionales Datum, bis wann diese Qualifikation für die Person gültig ist.
 * @property istAktiv Gibt an, ob diese Qualifikation für die Person aktuell als aktiv betrachtet wird.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class DomQualifikation(
    @Serializable(with = UuidSerializer::class)
    val qualifikationId: Uuid = uuid4(),

    @Serializable(with = UuidSerializer::class)
    val personId: Uuid, // FK zu DomPerson.personId

    @Serializable(with = UuidSerializer::class)
    val qualTypId: Uuid, // FK zu QualifikationsTyp.qualTypId

    var bemerkung: String? = null,

    @Serializable(with = KotlinLocalDateSerializer::class)
    var gueltigVon: LocalDate? = null, // Nicht direkt in RICHT01.dat, aber evtl. für manuelle Pflege

    @Serializable(with = KotlinLocalDateSerializer::class)
    var gueltigBis: LocalDate? = null, // Nicht direkt in RICHT01.dat

    var istAktiv: Boolean = true, // Standardmäßig aktiv, wenn importiert

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
