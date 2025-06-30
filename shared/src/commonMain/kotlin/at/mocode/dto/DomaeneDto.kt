package at.mocode.dto

import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.KotlinLocalDateSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

// DomLizenz DTOs
@Serializable
data class DomLizenzDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val lizenzTyp: String,
    val bezeichnung: String,
    val beschreibung: String?,
    val sparte: String?,
    val mindestalter: Int?,
    val voraussetzungen: String?,
    val gueltigkeitsdauerJahre: Int?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateDomLizenzDto(
    val lizenzTyp: String,
    val bezeichnung: String,
    val beschreibung: String? = null,
    val sparte: String? = null,
    val mindestalter: Int? = null,
    val voraussetzungen: String? = null,
    val gueltigkeitsdauerJahre: Int? = null,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdateDomLizenzDto(
    val lizenzTyp: String,
    val bezeichnung: String,
    val beschreibung: String? = null,
    val sparte: String? = null,
    val mindestalter: Int? = null,
    val voraussetzungen: String? = null,
    val gueltigkeitsdauerJahre: Int? = null,
    val istAktiv: Boolean = true
)

// DomPerson DTOs
@Serializable
data class DomPersonDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val nachname: String,
    val vorname: String,
    val titel: String?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val geburtsdatum: LocalDate?,
    val geschlecht: String?,
    val nationalitaet: String?,
    val email: String?,
    val telefon: String?,
    val adresse: String?,
    val plz: String?,
    val ort: String?,
    val land: String?,
    val feiId: String?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateDomPersonDto(
    val nachname: String,
    val vorname: String,
    val titel: String? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val geburtsdatum: LocalDate? = null,
    val geschlecht: String? = null,
    val nationalitaet: String? = null,
    val email: String? = null,
    val telefon: String? = null,
    val adresse: String? = null,
    val plz: String? = null,
    val ort: String? = null,
    val land: String? = null,
    val feiId: String? = null,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdateDomPersonDto(
    val nachname: String,
    val vorname: String,
    val titel: String? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val geburtsdatum: LocalDate? = null,
    val geschlecht: String? = null,
    val nationalitaet: String? = null,
    val email: String? = null,
    val telefon: String? = null,
    val adresse: String? = null,
    val plz: String? = null,
    val ort: String? = null,
    val land: String? = null,
    val feiId: String? = null,
    val istAktiv: Boolean = true
)

// DomPferd DTOs
@Serializable
data class DomPferdDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val name: String,
    val rasse: String?,
    val farbe: String?,
    val geschlecht: String?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val geburtsdatum: LocalDate?,
    val geburtsland: String?,
    val vater: String?,
    val mutter: String?,
    val zuechter: String?,
    val eigentuemer: String?,
    val feiId: String?,
    val lebensnummer: String?,
    val chipNummer: String?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateDomPferdDto(
    val name: String,
    val rasse: String? = null,
    val farbe: String? = null,
    val geschlecht: String? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val geburtsdatum: LocalDate? = null,
    val geburtsland: String? = null,
    val vater: String? = null,
    val mutter: String? = null,
    val zuechter: String? = null,
    val eigentuemer: String? = null,
    val feiId: String? = null,
    val lebensnummer: String? = null,
    val chipNummer: String? = null,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdateDomPferdDto(
    val name: String,
    val rasse: String? = null,
    val farbe: String? = null,
    val geschlecht: String? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val geburtsdatum: LocalDate? = null,
    val geburtsland: String? = null,
    val vater: String? = null,
    val mutter: String? = null,
    val zuechter: String? = null,
    val eigentuemer: String? = null,
    val feiId: String? = null,
    val lebensnummer: String? = null,
    val chipNummer: String? = null,
    val istAktiv: Boolean = true
)

// DomQualifikation DTOs
@Serializable
data class DomQualifikationDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val bezeichnung: String,
    val beschreibung: String?,
    val kategorie: String?,
    val sparte: String?,
    val voraussetzungen: String?,
    val gueltigkeitsdauerJahre: Int?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateDomQualifikationDto(
    val bezeichnung: String,
    val beschreibung: String? = null,
    val kategorie: String? = null,
    val sparte: String? = null,
    val voraussetzungen: String? = null,
    val gueltigkeitsdauerJahre: Int? = null,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdateDomQualifikationDto(
    val bezeichnung: String,
    val beschreibung: String? = null,
    val kategorie: String? = null,
    val sparte: String? = null,
    val voraussetzungen: String? = null,
    val gueltigkeitsdauerJahre: Int? = null,
    val istAktiv: Boolean = true
)

// DomVerein DTOs
@Serializable
data class DomVereinDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val name: String,
    val kuerzel: String?,
    val adresse: String?,
    val plz: String?,
    val ort: String?,
    val land: String?,
    val email: String?,
    val telefon: String?,
    val webseite: String?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateDomVereinDto(
    val name: String,
    val kuerzel: String? = null,
    val adresse: String? = null,
    val plz: String? = null,
    val ort: String? = null,
    val land: String? = null,
    val email: String? = null,
    val telefon: String? = null,
    val webseite: String? = null,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdateDomVereinDto(
    val name: String,
    val kuerzel: String? = null,
    val adresse: String? = null,
    val plz: String? = null,
    val ort: String? = null,
    val land: String? = null,
    val email: String? = null,
    val telefon: String? = null,
    val webseite: String? = null,
    val istAktiv: Boolean = true
)
