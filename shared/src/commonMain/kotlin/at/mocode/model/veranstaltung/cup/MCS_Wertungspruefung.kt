import at.mocode.serializers.BigDecimalSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.Serializable

/**
 * Verknüpft eine Meisterschaft/Cup/Serie (`Meisterschaft_Cup_Serie`) mit einer spezifischen
 * Prüfungsabteilung (`Pruefung_Abteilung`), die als Wertungsprüfung für diesen
 * übergreifenden Wettbewerb zählt.
 *
 * @property mcsId Fremdschlüssel zur `Meisterschaft_Cup_Serie`. Teil des zusammengesetzten Primärschlüssels.
 * @property pruefungAbteilungDbId Fremdschlüssel zur `Pruefung_Abteilung`. Teil des zusammengesetzten Primärschlüssels.
 * @property faktorFuerWertung Ein optionaler Faktor, mit dem das Ergebnis dieser Wertungsprüfung
 * in die Gesamtwertung des Cups/der Meisterschaft einfließt (Default ist 1.0).
 * @property bemerkung Optionale Bemerkungen zu dieser spezifischen Wertungsprüfung im Kontext des Cups
 * (z.B. "1. Vorrunde", "Finale", "Qualifikation West").
 * @property istPflichttermin Gibt an, ob die Teilnahme an dieser Wertungsprüfung für die Cup-Gesamtwertung verpflichtend ist.
 * @property mindestErgebnisNotwendig Optionales Mindestergebnis, das in dieser Prüfung erzielt werden muss,
 * um für den Cup gewertet zu werden oder sich für das Finale zu qualifizieren.
 */
@Serializable
data class MCS_Wertungspruefung(
    @Serializable(with = UuidSerializer::class)
    val mcsId: Uuid, // Teil des PK, FK zu Meisterschaft_Cup_Serie.mcsId

    @Serializable(with = UuidSerializer::class)
    val pruefungAbteilungDbId: Uuid,   // Teil des PK, FK zu Pruefung_Abteilung.pruefungAbteilungDbId

    @Serializable(with = BigDecimalSerializer::class)
    var faktorFuerWertung: BigDecimal? = BigDecimal.fromInt(1), // Default 1.0
    var bemerkung: String? = null,
    var istPflichttermin: Boolean = false,
    var mindestErgebnisNotwendig: String? = null // z.B. "Note 6.0", "Fehlerfrei" - muss interpretiert werden
)
