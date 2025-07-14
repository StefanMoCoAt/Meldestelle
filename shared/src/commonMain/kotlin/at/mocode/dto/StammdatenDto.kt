package at.mocode.dto

import at.mocode.enums.FunktionaerRolleE
import at.mocode.enums.GeschlechtE
import at.mocode.stammdaten.LizenzInfo
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.KotlinLocalDateSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

// Person DTOs
@Serializable
data class PersonDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val oepsSatzNr: String?,
    val nachname: String,
    val vorname: String,
    val titel: String?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val geburtsdatum: LocalDate?,
    val geschlechtE: GeschlechtE?,
    val nationalitaet: String?,
    val email: String?,
    val telefon: String?,
    val adresse: String?,
    val plz: String?,
    val ort: String?,
    @Serializable(with = UuidSerializer::class)
    val stammVereinId: Uuid?,
    val mitgliedsNummerIntern: String?,
    val letzteZahlungJahr: Int?,
    val feiId: String?,
    val istGesperrt: Boolean,
    val sperrGrund: String?,
    val rollen: Set<FunktionaerRolleE>,
    val lizenzen: List<LizenzInfo>,
    val qualifikationenRichter: List<String>,
    val qualifikationenParcoursbauer: List<String>,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreatePersonDto(
    val oepsSatzNr: String? = null,
    val nachname: String,
    val vorname: String,
    val titel: String? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val geburtsdatum: LocalDate? = null,
    val geschlechtE: GeschlechtE? = null,
    val nationalitaet: String? = null,
    val email: String? = null,
    val telefon: String? = null,
    val adresse: String? = null,
    val plz: String? = null,
    val ort: String? = null,
    @Serializable(with = UuidSerializer::class)
    val stammVereinId: Uuid? = null,
    val mitgliedsNummerIntern: String? = null,
    val letzteZahlungJahr: Int? = null,
    val feiId: String? = null,
    val istGesperrt: Boolean = false,
    val sperrGrund: String? = null,
    val rollen: Set<FunktionaerRolleE> = emptySet(),
    val lizenzen: List<LizenzInfo> = emptyList(),
    val qualifikationenRichter: List<String> = emptyList(),
    val qualifikationenParcoursbauer: List<String> = emptyList(),
    val istAktiv: Boolean = true
)

@Serializable
data class UpdatePersonDto(
    val oepsSatzNr: String? = null,
    val nachname: String,
    val vorname: String,
    val titel: String? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val geburtsdatum: LocalDate? = null,
    val geschlechtE: GeschlechtE? = null,
    val nationalitaet: String? = null,
    val email: String? = null,
    val telefon: String? = null,
    val adresse: String? = null,
    val plz: String? = null,
    val ort: String? = null,
    @Serializable(with = UuidSerializer::class)
    val stammVereinId: Uuid? = null,
    val mitgliedsNummerIntern: String? = null,
    val letzteZahlungJahr: Int? = null,
    val feiId: String? = null,
    val istGesperrt: Boolean = false,
    val sperrGrund: String? = null,
    val rollen: Set<FunktionaerRolleE> = emptySet(),
    val lizenzen: List<LizenzInfo> = emptyList(),
    val qualifikationenRichter: List<String> = emptyList(),
    val qualifikationenParcoursbauer: List<String> = emptyList(),
    val istAktiv: Boolean = true
)

// Pferd DTOs
@Serializable
data class PferdDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val oepsPferdNr: String?,
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
    @Serializable(with = UuidSerializer::class)
    val heimatVereinId: Uuid?,
    val feiId: String?,
    val lebensnummer: String?,
    val chipNummer: String?,
    val istGesperrt: Boolean,
    val sperrGrund: String?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreatePferdDto(
    val oepsPferdNr: String? = null,
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
    @Serializable(with = UuidSerializer::class)
    val heimatVereinId: Uuid? = null,
    val feiId: String? = null,
    val lebensnummer: String? = null,
    val chipNummer: String? = null,
    val istGesperrt: Boolean = false,
    val sperrGrund: String? = null,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdatePferdDto(
    val oepsPferdNr: String? = null,
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
    @Serializable(with = UuidSerializer::class)
    val heimatVereinId: Uuid? = null,
    val feiId: String? = null,
    val lebensnummer: String? = null,
    val chipNummer: String? = null,
    val istGesperrt: Boolean = false,
    val sperrGrund: String? = null,
    val istAktiv: Boolean = true
)

// LizenzInfo DTOs
@Serializable
data class LizenzInfoDto(
    val lizenzTyp: String,
    val gueltigVon: LocalDate?,
    val gueltigBis: LocalDate?,
    val istAktiv: Boolean
)

@Serializable
data class CreateLizenzInfoDto(
    val lizenzTyp: String,
    val gueltigVon: LocalDate? = null,
    val gueltigBis: LocalDate? = null,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdateLizenzInfoDto(
    val lizenzTyp: String,
    val gueltigVon: LocalDate? = null,
    val gueltigBis: LocalDate? = null,
    val istAktiv: Boolean = true
)

// BundeslandDefinition DTOs
@Serializable
data class BundeslandDefinitionDto(
    val code: String,
    val bezeichnung: String,
    val land: String,
    val istAktiv: Boolean
)

@Serializable
data class CreateBundeslandDefinitionDto(
    val code: String,
    val bezeichnung: String,
    val land: String,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdateBundeslandDefinitionDto(
    val code: String,
    val bezeichnung: String,
    val land: String,
    val istAktiv: Boolean = true
)

// LandDefinition DTOs
@Serializable
data class LandDefinitionDto(
    val code: String,
    val bezeichnung: String,
    val istEuMitglied: Boolean,
    val istAktiv: Boolean
)

@Serializable
data class CreateLandDefinitionDto(
    val code: String,
    val bezeichnung: String,
    val istEuMitglied: Boolean = false,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdateLandDefinitionDto(
    val code: String,
    val bezeichnung: String,
    val istEuMitglied: Boolean = false,
    val istAktiv: Boolean = true
)
