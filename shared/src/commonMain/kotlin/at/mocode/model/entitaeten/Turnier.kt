package at.mocode.model.entitaeten

import at.mocode.model.enums.NennungsArt
import at.mocode.model.serializers.*
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Turnier(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    var veranstaltungId: Uuid,
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
    @Serializable(with = UuidSerializer::class)
    var turnierleiterId: Uuid?, // FK zu Person
    @Serializable(with = UuidSerializer::class)
    var turnierbeauftragterId: Uuid?, // FK zu Person
    var richterIds: List<@Serializable(with = UuidSerializer::class) Uuid> = emptyList(), // Pool an Richtern
    var parcoursbauerIds: List<@Serializable(with = UuidSerializer::class) Uuid> = emptyList(), // FKs zu Person
    var parcoursAssistentIds: List<@Serializable(with = UuidSerializer::class) Uuid> = emptyList(), // FKs zu Person
    var tierarztInfos: String?,
    var hufschmiedInfo: String?,
    @Serializable(with = UuidSerializer::class)
    var meldestelleVerantwortlicherId: Uuid?, // FK zu Person
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
