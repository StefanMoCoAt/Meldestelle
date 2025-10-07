@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.domain.model

import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Definiert ein Bundesland oder eine vergleichbare subnationale Verwaltungseinheit.
 *
 * Diese Entität ist primär für die österreichischen Bundesländer mit ihren OEPS-spezifischen
 * Codes gedacht, kann aber auch für Bundesländer/Regionen anderer Nationen erweitert werden.
 *
 * @property bundeslandId Eindeutiger interner Identifikator für dieses Bundesland (UUID).
 * @property landId Fremdschlüssel zur `LandDefinition`, dem dieses Bundesland angehört.
 * @property oepsCode Der 2-stellige numerische OEPS-Code für österreichische Bundesländer
 * (z.B. "01" für Wien, "02" für Niederösterreich). Sollte eindeutig sein für Land "Österreich".
 * @property iso3166_2_Code Optionaler offizieller ISO 3166-2 Code für das Bundesland
 * (z.B. "AT-1" für Burgenland, "DE-BY" für Bayern).
 * @property name Der offizielle Name des Bundeslandes.
 * @property kuerzel Ein gängiges Kürzel für das Bundesland (z.B. "NÖ", "W", "STMK").
 * @property wappenUrl Optionaler URL-Pfad zu einer Bilddatei des Bundeslandwappens.
 * @property istAktiv Gibt an, ob dieses Bundesland aktuell im System ausgewählt/verwendet werden kann.
 * @property sortierReihenfolge Optionale Zahl zur Steuerung der Sortierreihenfolge in Auswahllisten.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class BundeslandDefinition(
    @Serializable(with = UuidSerializer::class)
    val bundeslandId: Uuid = Uuid.random(),

    @Serializable(with = UuidSerializer::class)
    var landId: Uuid, // FK zu LandDefinition.landId

    var oepsCode: String?,    // z.B. "01", "02", ... für Österreich; eindeutig pro landId = Österreich
    var iso3166_2_Code: String?, // z.B. "AT-1", "DE-BY"; Eindeutig global oder pro Land?
    var name: String,         // z.B. "Niederösterreich", "Bayern"
    var kuerzel: String? = null,     // z.B. "NÖ", "BY"
    var wappenUrl: String? = null,
    var istAktiv: Boolean = true,
    var sortierReihenfolge: Int? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
