package at.mocode.model.entitaeten

// Hinweis: In einem Kotlin Multiplatform-Projekt können JVM-spezifische Klassen wie
// java.math.BigDecimal und java.util.UUID nicht im commonMain-Quellcode verwendet werden.
// Stattdessen werden multiplatformfähige Alternativen verwendet:
// - com.benasher44.uuid.Uuid anstelle von java.util.UUID
// - com.ionspin.kotlin.bignum.decimal.BigDecimal anstelle von java.math.BigDecimal
// Diese Klassen bieten ähnliche Funktionalität, sind aber auf allen Plattformen verfügbar.
//
// Für JVM-spezifischen Code können diese Klassen im jvmMain-Quellset verwendet werden.
// Siehe: shared/src/jvmMain/kotlin/at/mocode/model/JvmSerializer.kt

import at.mocode.model.enums.NennungsArt
import at.mocode.model.serializer.*
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import com.benasher44.uuid.Uuid as UUID

@Serializable
data class Turnier(
    @Serializable(with = JavaUUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    @Serializable(with = JavaUUIDSerializer::class)
    var veranstaltungId: UUID,
    var oepsTurnierNr: String,
    var titel: String,
    var untertitel: String?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var datumBis: LocalDate,
    @Serializable(with = KotlinLocalDateTimeSerializer::class) // Beispiel
    var nennungsschluss: LocalDateTime?,
    var nennungsArt: List<NennungsArt> = emptyList(),
    var nennungsHinweis: String?,
    var eigenesNennsystemUrl: String?,
    @Serializable(with = BigDecimalSerializer::class)
    var nenngeld: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    var startgeldStandard: BigDecimal?,
    var austragungsplaetze: List<Platz> = emptyList(),
    var vorbereitungsplaetze: List<Platz> = emptyList(),
    @Serializable(with = JavaUUIDSerializer::class)
    var turnierleiterId: UUID?, // FK zu Person
    @Serializable(with = JavaUUIDSerializer::class)
    var turnierbeauftragterId: UUID?, // FK zu Person
    var richterIds: List<@Serializable(with = JavaUUIDSerializer::class)UUID> = emptyList(), // Pool an Richtern
    var parcoursbauerIds: List<@Serializable(with = JavaUUIDSerializer::class)UUID> = emptyList(), // FKs zu Person
    var parcoursAssistentIds: List<@Serializable(with = JavaUUIDSerializer::class)UUID> = emptyList(), // FKs zu Person
    var tierarztInfos: String?,
    var hufschmiedInfo: String?,
    @Serializable(with = JavaUUIDSerializer::class)
    var meldestelleVerantwortlicherId: UUID?, // FK zu Person
    var meldestelleTelefon: String?,
    var meldestelleOeffnungszeiten: String?,
    var ergebnislistenUrl: String?, // Wird später meist system-generiert
    var verfuegbareArtikel: List<Artikel> = emptyList(), // Zur Auswahl für die Kassa
    var meisterschaftRefs: List<MeisterschaftReferenz> = emptyList(),
    // var cupRefs: List<CupReferenz> = emptyList(),
    // var sonderpruefungRefs: List<SonderpruefungReferenz> = emptyList(),
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
