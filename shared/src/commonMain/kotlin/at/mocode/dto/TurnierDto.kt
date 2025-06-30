package at.mocode.dto

import at.mocode.enums.NennungsArtE
import at.mocode.model.Artikel
import at.mocode.model.MeisterschaftReferenz
import at.mocode.model.Platz
import at.mocode.serializers.*
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class TurnierDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val veranstaltungId: Uuid,
    val oepsTurnierNr: String,
    val titel: String,
    val untertitel: String?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumBis: LocalDate,
    @Serializable(with = KotlinLocalDateTimeSerializer::class)
    val nennungsschluss: LocalDateTime?,
    val nennungsArt: List<NennungsArtE>,
    val nennungsHinweis: String?,
    val eigenesNennsystemUrl: String?,
    @Serializable(with = BigDecimalSerializer::class)
    val nenngeld: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeldStandard: BigDecimal?,
    val austragungsplaetze: List<Platz>,
    val vorbereitungsplaetze: List<Platz>,
    @Serializable(with = UuidSerializer::class)
    val turnierleiterId: Uuid?,
    @Serializable(with = UuidSerializer::class)
    val turnierbeauftragterId: Uuid?,
    val richterIds: List<@Serializable(with = UuidSerializer::class) Uuid>,
    val parcoursbauerIds: List<@Serializable(with = UuidSerializer::class) Uuid>,
    val parcoursAssistentIds: List<@Serializable(with = UuidSerializer::class) Uuid>,
    val tierarztInfos: String?,
    val hufschmiedInfo: String?,
    @Serializable(with = UuidSerializer::class)
    val meldestelleVerantwortlicherId: Uuid?,
    val meldestelleTelefon: String?,
    val meldestelleOeffnungszeiten: String?,
    val ergebnislistenUrl: String?,
    val verfuegbareArtikel: List<Artikel>,
    val meisterschaftRefs: List<MeisterschaftReferenz>,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateTurnierDto(
    @Serializable(with = UuidSerializer::class)
    val veranstaltungId: Uuid,
    val oepsTurnierNr: String,
    val titel: String,
    val untertitel: String? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumBis: LocalDate,
    @Serializable(with = KotlinLocalDateTimeSerializer::class)
    val nennungsschluss: LocalDateTime? = null,
    val nennungsArt: List<NennungsArtE> = emptyList(),
    val nennungsHinweis: String? = null,
    val eigenesNennsystemUrl: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val nenngeld: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeldStandard: BigDecimal? = null,
    val austragungsplaetze: List<Platz> = emptyList(),
    val vorbereitungsplaetze: List<Platz> = emptyList(),
    @Serializable(with = UuidSerializer::class)
    val turnierleiterId: Uuid? = null,
    @Serializable(with = UuidSerializer::class)
    val turnierbeauftragterId: Uuid? = null,
    val richterIds: List<@Serializable(with = UuidSerializer::class) Uuid> = emptyList(),
    val parcoursbauerIds: List<@Serializable(with = UuidSerializer::class) Uuid> = emptyList(),
    val parcoursAssistentIds: List<@Serializable(with = UuidSerializer::class) Uuid> = emptyList(),
    val tierarztInfos: String? = null,
    val hufschmiedInfo: String? = null,
    @Serializable(with = UuidSerializer::class)
    val meldestelleVerantwortlicherId: Uuid? = null,
    val meldestelleTelefon: String? = null,
    val meldestelleOeffnungszeiten: String? = null,
    val ergebnislistenUrl: String? = null,
    val verfuegbareArtikel: List<Artikel> = emptyList(),
    val meisterschaftRefs: List<MeisterschaftReferenz> = emptyList()
)

@Serializable
data class UpdateTurnierDto(
    val oepsTurnierNr: String,
    val titel: String,
    val untertitel: String? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumBis: LocalDate,
    @Serializable(with = KotlinLocalDateTimeSerializer::class)
    val nennungsschluss: LocalDateTime? = null,
    val nennungsArt: List<NennungsArtE> = emptyList(),
    val nennungsHinweis: String? = null,
    val eigenesNennsystemUrl: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val nenngeld: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeldStandard: BigDecimal? = null,
    val austragungsplaetze: List<Platz> = emptyList(),
    val vorbereitungsplaetze: List<Platz> = emptyList(),
    @Serializable(with = UuidSerializer::class)
    val turnierleiterId: Uuid? = null,
    @Serializable(with = UuidSerializer::class)
    val turnierbeauftragterId: Uuid? = null,
    val richterIds: List<@Serializable(with = UuidSerializer::class) Uuid> = emptyList(),
    val parcoursbauerIds: List<@Serializable(with = UuidSerializer::class) Uuid> = emptyList(),
    val parcoursAssistentIds: List<@Serializable(with = UuidSerializer::class) Uuid> = emptyList(),
    val tierarztInfos: String? = null,
    val hufschmiedInfo: String? = null,
    @Serializable(with = UuidSerializer::class)
    val meldestelleVerantwortlicherId: Uuid? = null,
    val meldestelleTelefon: String? = null,
    val meldestelleOeffnungszeiten: String? = null,
    val ergebnislistenUrl: String? = null,
    val verfuegbareArtikel: List<Artikel> = emptyList(),
    val meisterschaftRefs: List<MeisterschaftReferenz> = emptyList()
)
