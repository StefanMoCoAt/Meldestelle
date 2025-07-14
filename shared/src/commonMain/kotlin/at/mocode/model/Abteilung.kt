package at.mocode.model

import at.mocode.enums.BeginnzeitTypE
import at.mocode.serializers.BigDecimalSerializer
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.KotlinLocalTimeSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class Abteilung(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    val bewerbId: Uuid, // Gehört zu diesem Hauptbewerb

    var abteilungsKennzeichen: String, // z.B. "1", "A", "R1", oder generiert "Abt1"
    var bezeichnungIntern: String?,    // Zur Unterscheidung im Admin, z.B. "R1 Reiter", "Lizenzfrei", "Pony LK3"
    var bezeichnungAufStartliste: String?, // Wie es auf der Start/Ergebnisliste erscheinen soll

    // Kriterien für diese Abteilung (aus SUDO "Teilen nach:")
    var teilungsKriteriumLizenz: String? = null, // z.B. "R1", "R2+R3", "lizenzfrei" (kann komplex sein)
    var teilungsKriteriumPferdealter: String? = null,
    var teilungsKriteriumAltersklasseReiter: String? = null,
    var teilungsKriteriumAnzahlMin: Int? = null, // Mindestanzahl für diese Teilung
    var teilungsKriteriumAnzahlMax: Int? = null, // Maximale Anzahl für diese Teilung
    var teilungsKriteriumFreiText: String? = null, // Für "freie Angabe"

    // Überschreibt ggf. Werte vom Hauptbewerb
    @Serializable(with = BigDecimalSerializer::class)
    var startgeld: BigDecimal? = null,
    var dotierungen: List<DotierungsAbstufung> = emptyList(),
    @Serializable(with = UuidSerializer::class)
    var platzId: Uuid?, // FK zum Austragungsplatz (kann vom Hauptbewerb abweichen)
    var datum: LocalDate?,
    var beginnzeitTypE: BeginnzeitTypE = BeginnzeitTypE.ANSCHLIESSEND,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    var beginnzeitFix: LocalTime? = null,
    @Serializable(with = UuidSerializer::class)
    var beginnNachAbteilungId: Uuid?, // Reihenfolge der Abteilungen
    var beginnzeitCa: LocalTime? = null,
    var dauerProStartGeschaetztSek: Int? = null,
    var umbauzeitNachAbteilungMin: Int? = null,
    var besichtigungszeitVorAbteilungMin: Int? = null,
    var stechzeitZusaetzlichMin: Int? = null,

    var anzahlStarter: Int = 0, // Wird später befüllt
    var istAktiv: Boolean = true, // Kann diese Abteilung genannt werden?

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
