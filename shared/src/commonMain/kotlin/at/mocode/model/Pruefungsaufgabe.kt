package at.mocode.model

import at.mocode.enums.SparteE
import at.mocode.enums.PruefungsaufgabeNationE
import at.mocode.enums.PruefungsaufgabeRichtverfahrenModusE
import at.mocode.enums.PruefungsaufgabeViereckE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Pruefungsaufgabe(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    var kuerzel: String, // Eindeutiges Kürzel, z.B. "A1", "LF3", "FEI GP PSG"
    var nameLang: String, // Vollständiger Name, z.B. "Dressuraufgabe A1 (GM, 20x40m)"
    var kategorieText: String?, // Übergeordnete Kategorie, z.B. "Dressuraufgaben Klasse A", "FEI Grand Prix Serie"
    var sparteE: SparteE, // Primär DRESSUR, aber auch für Vielseitigkeit etc.
    var nation: PruefungsaufgabeNationE = PruefungsaufgabeNationE.NATIONAL,
    var richtverfahrenModusDefault: PruefungsaufgabeRichtverfahrenModusE?, // GM, GT - als Default für diese Aufgabe
    var viereckGroesseDefault: PruefungsaufgabeViereckE?, // VIERECK_20x40, VIERECK_20x60 - als Default
    var schwierigkeitsgradText: String?, // z.B. "A", "L", "M", "S", "Grand Prix"
    var aufgabenNummerInSammlung: String?, // z.B. die "1" bei "Aufgabe A1" oder spezifische FEI Nummer
    var jahrgangVersion: String?, // z.B. "2011", "FEI 2023"
    var pdfUrlExtern: String?, // Link zur offiziellen PDF-Datei (OEPS, FEI)
    var pdfDateinameIntern: String?, // Falls wir die PDFs auch bei uns speichern/hochladen (Dateiname)
    var anmerkungen: String?, // z.B. "Diese Aufgabe darf nur mit Leitfaden gerichtet werden"
    var dauerGeschaetztMinuten: Double?, // z.B. 3.5
    var anzahlMaxPunkteProRichter: Double?, // Für Dressur, falls standardisiert
    var istAktiv: Boolean = true, // Kann die Aufgabe aktuell ausgewählt werden?
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
