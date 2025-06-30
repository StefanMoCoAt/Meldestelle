package at.mocode.model.oeto_verwaltung

import at.mocode.enums.SparteE // Optional, falls Altersklassen stark spartenspezifisch sind
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Definiert eine spezifische Altersklasse für Teilnehmer (Reiter, Fahrer, Voltigierer)
 * oder ggf. auch für Pferde, basierend auf den Vorgaben der ÖTO oder anderer Regelwerke.
 *
 * Beispiele: "Jugend U16", "Junioren U18", "Junge Reiter U21", "Allgemeine Klasse",
 * "Pony Jugend U14", "Senioren Ü40".
 * Diese Definitionen dienen zur Überprüfung von Teilnahmeberechtigungen in Bewerben und Abteilungen.
 *
 * @property altersklasseId Eindeutiger interner Identifikator für diese Altersklassendefinition (UUID).
 * @property altersklasseCode Ein eindeutiges Kürzel oder Code für die Altersklasse
 * (z.B. "JGD_U16", "JUN_U18", "YR_U21", "AK", "PONY_U14"). Dient als fachlicher Schlüssel.
 * @property bezeichnung Die offizielle oder allgemein verständliche Bezeichnung der Altersklasse.
 * @property minAlter Das Mindestalter (Jahre, inklusive) für diese Altersklasse. `null`, wenn es keine Untergrenze gibt.
 * @property maxAlter Das Höchstalter (Jahre, inklusive) für diese Altersklasse. `null`, wenn es keine Obergrenze gibt.
 * @property stichtagRegelText Eine Beschreibung der Regel für den Stichtag zur Altersberechnung
 * (z.B. "31.12. des laufenden Kalenderjahres", "Geburtstag im laufenden Jahr").
 * @property sparteFilter Optionale Angabe, ob diese Altersklassendefinition nur für eine spezifische Sparte gilt.
 * @property geschlechtFilter Optionaler Filter für das Geschlecht ('M', 'W'), falls die Altersklasse geschlechtsspezifisch ist.
 * `null` bedeutet für alle Geschlechter gültig.
 * @property oetoRegelReferenzId Optionale Verknüpfung zu einer spezifischen Regel in der `OETORegelReferenz`-Tabelle,
 * die diese Altersklasse definiert.
 * @property istAktiv Gibt an, ob diese Altersklassendefinition aktuell im System verwendet werden kann.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class AltersklasseDefinition(
    @Serializable(with = UuidSerializer::class)
    val altersklasseId: Uuid = uuid4(), // Interner Primärschlüssel

    var altersklasseCode: String,       // Fachlicher PK, z.B. "JGD_U16"
    var bezeichnung: String,
    var minAlter: Int? = null,
    var maxAlter: Int? = null,
    var stichtagRegelText: String? = "31.12. des laufenden Kalenderjahres", // Typischer Default
    var sparteFilter: SparteE? = null, // Ist diese Definition spartenspezifisch?
    var geschlechtFilter: Char? = null, // 'M', 'W', oder null für beide

    @Serializable(with = UuidSerializer::class)
    var oetoRegelReferenzId: Uuid? = null, // FK zu OETORegelReferenz.oetoRegelReferenzId

    var istAktiv: Boolean = true,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
