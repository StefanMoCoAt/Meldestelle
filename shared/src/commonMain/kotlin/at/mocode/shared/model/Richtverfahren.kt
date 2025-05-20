package at.mocode.shared.model

import at.mocode.shared.enums.SparteE
import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Richtverfahren(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    var code: String, // Eindeutiger Code, z.B. "A", "A2_OETO204", "C_ZEIT", "STIL_A_IDEALZEIT"
    var bezeichnung: String, // z.B. "Standardspringprüfung nach Fehlern und Zeit (§204 A2)", "Stilspringprüfung Kl. A mit Idealzeit"
    var sparteE: SparteE,
    var basisRegelnBeschreibungKurz: String?, // Kurze Beschreibung oder Hauptmerkmal
    var oetoParagraphVerweis: String?, // z.B. "ÖTO §204 A2", "ÖTO §104"
    var hatStechen: Boolean = false,
    var artDesStechens: String? = null, // z.B. "nach Fehlern/Zeit", "nur Fehler"
    // Weitere strukturierte Regeln könnten hier folgen, z.B. für Fehlerpunkte, Zeitberechnung.
    // Für V1 könnten viele Regeln noch in der Anwendungslogik sein, basierend auf dem `code`.
    // Beispiel für strukturierte Fehler:
    // var fehlerdefinitionen: Map<String, Double> = emptyMap(), // z.B. "HINDERNIS" -> 4.0
    var istAktiv: Boolean = true,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
