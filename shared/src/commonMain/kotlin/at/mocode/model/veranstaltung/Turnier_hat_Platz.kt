import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * Verknüpft ein Turnier (`Turnier_OEPS`) mit einem genutzten Platz (`Platz`)
 * und beschreibt den spezifischen Verwendungszweck dieses Platzes im Kontext des Turniers.
 *
 * @property turnierOepsId Fremdschlüssel zum `Turnier_OEPS`. Teil des zusammengesetzten Primärschlüssels.
 * @property platzId Fremdschlüssel zum `Platz`. Teil des zusammengesetzten Primärschlüssels.
 * @property verwendungszweck Beschreibung, wofür der Platz bei diesem Turnier genutzt wird.
 * @property istHauptAustragungsplatz Optionales Flag, um den primären Austragungsplatz zu kennzeichnen.
 * @property istHauptVorbereitungsplatz Optionales Flag, um den primären Vorbereitungsplatz zu kennzeichnen.
 */
@Serializable
data class Turnier_hat_Platz(
    @Serializable(with = UuidSerializer::class)
    val turnierOepsId: Uuid, // Teil des PK, FK zu Turnier_OEPS.turnierOepsId

    @Serializable(with = UuidSerializer::class)
    val platzId: Uuid,   // Teil des PK, FK zu Platz.platzId

    var verwendungszweck: String? = null,
    var istHauptAustragungsplatz: Boolean? = false,
    var istHauptVorbereitungsplatz: Boolean? = false
)
