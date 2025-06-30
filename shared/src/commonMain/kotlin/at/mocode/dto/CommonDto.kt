package at.mocode.dto

import at.mocode.serializers.BigDecimalSerializer
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

// Pruefungsaufgabe DTOs
@Serializable
data class PruefungsaufgabeDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val bezeichnung: String,
    val beschreibung: String?,
    val kategorie: String?,
    val schwierigkeitsgrad: String?,
    val punkteMax: Int?,
    val zeitlimitSekunden: Int?,
    val istAktiv: Boolean,
    val notizen: String?,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreatePruefungsaufgabeDto(
    val bezeichnung: String,
    val beschreibung: String? = null,
    val kategorie: String? = null,
    val schwierigkeitsgrad: String? = null,
    val punkteMax: Int? = null,
    val zeitlimitSekunden: Int? = null,
    val istAktiv: Boolean = true,
    val notizen: String? = null
)

@Serializable
data class UpdatePruefungsaufgabeDto(
    val bezeichnung: String,
    val beschreibung: String? = null,
    val kategorie: String? = null,
    val schwierigkeitsgrad: String? = null,
    val punkteMax: Int? = null,
    val zeitlimitSekunden: Int? = null,
    val istAktiv: Boolean = true,
    val notizen: String? = null
)

// Richtverfahren DTOs
@Serializable
data class RichtverfahrenDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val bezeichnung: String,
    val beschreibung: String?,
    val kategorie: String?,
    val anzahlRichterErforderlich: Int,
    val bewertungsSchema: String?,
    val istStandardVerfahren: Boolean,
    val istAktiv: Boolean,
    val regelwerk: String?,
    val notizen: String?,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateRichtverfahrenDto(
    val bezeichnung: String,
    val beschreibung: String? = null,
    val kategorie: String? = null,
    val anzahlRichterErforderlich: Int = 1,
    val bewertungsSchema: String? = null,
    val istStandardVerfahren: Boolean = false,
    val istAktiv: Boolean = true,
    val regelwerk: String? = null,
    val notizen: String? = null
)

@Serializable
data class UpdateRichtverfahrenDto(
    val bezeichnung: String,
    val beschreibung: String? = null,
    val kategorie: String? = null,
    val anzahlRichterErforderlich: Int = 1,
    val bewertungsSchema: String? = null,
    val istStandardVerfahren: Boolean = false,
    val istAktiv: Boolean = true,
    val regelwerk: String? = null,
    val notizen: String? = null
)

// DotierungsAbstufung DTOs
@Serializable
data class DotierungsAbstufungDto(
    val platz: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val betrag: BigDecimal
)

// MeisterschaftReferenz DTOs
@Serializable
data class MeisterschaftReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val meisterschaftId: Uuid,
    val bezeichnung: String,
    val kategorie: String?
)

@Serializable
data class CreateMeisterschaftReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val meisterschaftId: Uuid,
    val bezeichnung: String,
    val kategorie: String? = null
)

@Serializable
data class UpdateMeisterschaftReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val meisterschaftId: Uuid,
    val bezeichnung: String,
    val kategorie: String? = null
)

// CupReferenz DTOs
@Serializable
data class CupReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val cupId: Uuid,
    val bezeichnung: String,
    val kategorie: String?
)

@Serializable
data class CreateCupReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val cupId: Uuid,
    val bezeichnung: String,
    val kategorie: String? = null
)

@Serializable
data class UpdateCupReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val cupId: Uuid,
    val bezeichnung: String,
    val kategorie: String? = null
)

// SonderpruefungReferenz DTOs
@Serializable
data class SonderpruefungReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val sonderpruefungId: Uuid,
    val bezeichnung: String,
    val kategorie: String?
)

@Serializable
data class CreateSonderpruefungReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val sonderpruefungId: Uuid,
    val bezeichnung: String,
    val kategorie: String? = null
)

@Serializable
data class UpdateSonderpruefungReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val sonderpruefungId: Uuid,
    val bezeichnung: String,
    val kategorie: String? = null
)

// Platz DTOs
@Serializable
data class PlatzDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val bezeichnung: String,
    val beschreibung: String?,
    val adresse: String?,
    val gpsKoordinaten: String?,
    val kapazitaet: Int?,
    val ausstattung: List<String>,
    val istVerfuegbar: Boolean,
    val kontaktInfo: String?,
    val notizen: String?,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreatePlatzDto(
    val bezeichnung: String,
    val beschreibung: String? = null,
    val adresse: String? = null,
    val gpsKoordinaten: String? = null,
    val kapazitaet: Int? = null,
    val ausstattung: List<String> = emptyList(),
    val istVerfuegbar: Boolean = true,
    val kontaktInfo: String? = null,
    val notizen: String? = null
)

@Serializable
data class UpdatePlatzDto(
    val bezeichnung: String,
    val beschreibung: String? = null,
    val adresse: String? = null,
    val gpsKoordinaten: String? = null,
    val kapazitaet: Int? = null,
    val ausstattung: List<String> = emptyList(),
    val istVerfuegbar: Boolean = true,
    val kontaktInfo: String? = null,
    val notizen: String? = null
)
