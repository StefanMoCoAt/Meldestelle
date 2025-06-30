package at.mocode.model.domaene

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
 * Repräsentiert eine spezifische Lizenz oder Qualifikation, die einer Person (`DomPerson`)
 * zugeordnet ist.
 *
 * Diese Entität verknüpft eine Person mit einer globalen Lizenzdefinition (`LizenzTypGlobal`)
 * und speichert zusätzliche Informationen wie die Gültigkeit.
 * Die Informationen stammen aus der Verarbeitung der Roh-Lizenzfelder
 * (insb. `lizenzinfoRawOepsRoh`) aus `Person_ZNS_Staging`.
 *
 * @property lizenzId Eindeutiger interner Identifikator für diese zugeordnete Lizenz (UUID).
 * @property personId Fremdschlüssel zur `DomPerson`, der diese Lizenz besitzt.
 * @property lizenzTypGlobalId Fremdschlüssel zum `LizenzTypGlobal`, der die Art dieser Lizenz definiert.
 * @property gueltigBisJahr Das Jahr, bis zu dem diese Lizenz (basierend auf der letzten Zahlung im ZNS) gültig ist.
 * @property ausgestelltAm Optionales Datum, an dem diese spezifische Lizenz für die Person ausgestellt wurde (nicht immer aus ZNS ersichtlich).
 * @property istAktivBezahltOeps Gibt an, ob diese Lizenz als "aktiv/bezahlt" aus den OEPS-Daten (primär LIZENZINFO) hervorgeht.
 * @property notiz Interne Anmerkungen zu dieser spezifischen Lizenzzuordnung.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class DomLizenz(
    @Serializable(with = UuidSerializer::class)
    val lizenzId: Uuid = uuid4(),

    @Serializable(with = UuidSerializer::class)
    val personId: Uuid, // FK zu DomPerson.personId

    @Serializable(with = UuidSerializer::class)
    val lizenzTypGlobalId: Uuid, // FK zu LizenzTypGlobal.lizenzTypGlobalId

    var gueltigBisJahr: Int?, // Aus Person_ZNS_Staging.jahrLetzteZahlungLizenzOepsRoh

    @Serializable(with = KotlinLocalDateSerializer::class)
    var ausgestelltAm: LocalDate? = null, // Nicht direkt in LIZENZ01.dat, ggf. manuell oder andere Quelle

    var istAktivBezahltOeps: Boolean = false, // Wird gesetzt, wenn die Lizenz aus LIZENZINFO stammt
    // oder als Hauptlizenz aktiv ist.

    var notiz: String? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
