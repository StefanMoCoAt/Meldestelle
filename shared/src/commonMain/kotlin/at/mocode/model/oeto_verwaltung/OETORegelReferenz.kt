package at.mocode.model.oeto_verwaltung

import at.mocode.enums.RegelwerkTypE
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
 * Repräsentiert einen spezifischen Verweis auf eine Regel, einen Paragrafen oder einen
 * Abschnitt innerhalb eines offiziellen Regelwerks, primär der österreichischen Turnierordnung (ÖTO)
 * oder des FEI-Reglements.
 *
 * Diese Entität dient dazu, Datenmodellentscheidungen, spezifische Anforderungen in Bewerben
 * oder Lizenzbedingungen nachvollziehbar mit der jeweiligen offiziellen Regelgrundlage
 * zu verknüpfen. Sie ermöglicht eine zentrale Verwaltung und Referenzierung von
 * Regelwerksbestandteilen.
 *
 * @property oetoRegelReferenzId Eindeutiger Identifikator für diesen Regelverweis (UUID).
 * @property paragraphNummer Die genaue Bezeichnung des Paragrafen, Artikels oder Abschnitts
 * (z.B. "§15", "§104 Abs. 2 Z1", "FEI Art. 240").
 * @property kapitelTitel Optionaler Titel des Kapitels oder übergeordneten Abschnitts,
 * in dem sich die Regel befindet (z.B. "Reiterlizenzen", "Richtverfahren A").
 * @property kurzbeschreibungRegel Optionale kurze Zusammenfassung oder Beschreibung des Inhalts der Regel.
 * @property regelwerkTyp Gibt an, auf welches Regelwerk sich dieser Verweis bezieht (z.B. ÖTO, FEI).
 * @property versionDatum Das Datum der Version des Regelwerks, auf das sich dieser Verweis bezieht,
 * um die Gültigkeit im Kontext der jeweiligen Regelwerksausgabe sicherzustellen.
 * @property urlDetail Optionaler URL-Link zur Online-Quelle der spezifischen Regel oder des Dokuments.
 * @property istAktiv Gibt an, ob dieser Regelverweis aktuell gültig und in Verwendung ist.
 * Veraltete Verweise können so markiert werden, ohne sie physisch zu löschen.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes in der lokalen Datenbank.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes in der lokalen Datenbank.
 */
@Serializable
data class OETORegelReferenz(

    @Serializable(with = UuidSerializer::class)
    val oetoRegelReferenzId: Uuid = uuid4(),

    var paragraphNummer: String,
    var kapitelTitel: String? = null,
    var kurzbeschreibungRegel: String? = null,
    var regelwerkTyp: RegelwerkTypE,     // OETO, FEI, SONSTIGE

    @Serializable(with = KotlinLocalDateSerializer::class)
    var versionDatum: LocalDate,

    var urlDetail: String? = null,
    var istAktiv: Boolean = true,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
