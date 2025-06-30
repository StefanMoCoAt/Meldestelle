package at.mocode.dto

import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.KotlinLocalDateSerializer
import at.mocode.serializers.KotlinLocalTimeSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

// Veranstaltung subdirectory DTOs

// Pruefung_OEPS DTOs
@Serializable
data class PruefungOepsDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val abteilungId: Uuid,
    val bezeichnung: String,
    val beschreibung: String?,
    val kategorie: String?,
    val schwierigkeitsgrad: String?,
    @Serializable(with = UuidSerializer::class)
    val richterIds: List<Uuid>,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datum: LocalDate?,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val startzeit: LocalTime?,
    @Serializable(with = UuidSerializer::class)
    val platzId: Uuid?,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

// Pruefung_Abteilung DTOs
@Serializable
data class PruefungAbteilungDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val pruefungId: Uuid,
    @Serializable(with = UuidSerializer::class)
    val abteilungId: Uuid,
    val reihenfolge: Int,
    val istAktiv: Boolean
)

// VeranstaltungsRahmen DTOs
@Serializable
data class VeranstaltungsRahmenDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val bezeichnung: String,
    val beschreibung: String?,
    val kategorie: String?,
    val regelwerk: String?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val gueltigVon: LocalDate?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val gueltigBis: LocalDate?,
    val istStandard: Boolean,
    @Serializable(with = UuidSerializer::class)
    val veranstalterId: Uuid?,
    @Serializable(with = UuidSerializer::class)
    val veranstaltungsortId: Uuid?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

// Turnier_hat_Platz DTOs
@Serializable
data class TurnierHatPlatzDto(
    @Serializable(with = UuidSerializer::class)
    val turnierId: Uuid,
    @Serializable(with = UuidSerializer::class)
    val platzId: Uuid,
    val verwendungszweck: String?
)

// OETO Verwaltung DTOs

// AltersklasseDefinition DTOs
@Serializable
data class AltersklasseDefinitionDto(
    val code: String,
    val bezeichnung: String,
    val minAlter: Int?,
    val maxAlter: Int?,
    val beschreibung: String?,
    val istAktiv: Boolean
)

// LizenzTypGlobal DTOs
@Serializable
data class LizenzTypGlobalDto(
    val code: String,
    val bezeichnung: String,
    val beschreibung: String?,
    val sparte: String?,
    val kategorie: String?,
    val istAktiv: Boolean
)

// QualifikationsTyp DTOs
@Serializable
data class QualifikationsTypDto(
    val code: String,
    val bezeichnung: String,
    val beschreibung: String?,
    val sparte: String?,
    val kategorie: String?,
    val istAktiv: Boolean
)

// Sportfachliche_Stammdaten DTOs
@Serializable
data class SportfachlicheStammdatenDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val typ: String,
    val code: String,
    val bezeichnung: String,
    val beschreibung: String?,
    val kategorie: String?,
    val sortierung: Int?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

// OETORegelReferenz DTOs
@Serializable
data class OetoRegelReferenzDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val regelCode: String,
    val bezeichnung: String,
    val beschreibung: String?,
    val kategorie: String?,
    val version: String?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val gueltigVon: LocalDate?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val gueltigBis: LocalDate?,
    val istAktiv: Boolean
)

// ZNS Staging DTOs

// Person_ZNS_Staging DTOs
@Serializable
data class PersonZnsStagingDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val znsSatzNr: String,
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
    val vereinsname: String?,
    val vereinsnummer: String?,
    val feiId: String?,
    val importDatum: Instant,
    val istVerarbeitet: Boolean,
    val verarbeitungsStatus: String?,
    val fehlerMeldung: String?
)

// Pferd_ZNS_Staging DTOs
@Serializable
data class PferdZnsStagingDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val znsPferdNr: String,
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
    val importDatum: Instant,
    val istVerarbeitet: Boolean,
    val verarbeitungsStatus: String?,
    val fehlerMeldung: String?
)

// Verein_ZNS_Staging DTOs
@Serializable
data class VereinZnsStagingDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val znsVereinsNr: String,
    val name: String,
    val kuerzel: String?,
    val adresse: String?,
    val plz: String?,
    val ort: String?,
    val bundesland: String?,
    val email: String?,
    val telefon: String?,
    val webseite: String?,
    val importDatum: Instant,
    val istVerarbeitet: Boolean,
    val verarbeitungsStatus: String?,
    val fehlerMeldung: String?
)

// Cup DTOs

// Meisterschaft_Cup_Serie DTOs
@Serializable
data class MeisterschaftCupSerieDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val bezeichnung: String,
    val beschreibung: String?,
    val saison: String,
    val kategorie: String?,
    val sparte: String?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val startDatum: LocalDate?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val endDatum: LocalDate?,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

// MCS_Wertungspruefung DTOs
@Serializable
data class McsWertungspruefungDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val meisterschaftCupSerieId: Uuid,
    @Serializable(with = UuidSerializer::class)
    val pruefungId: Uuid,
    val wertungsfaktor: Double?,
    val istPflichtpruefung: Boolean,
    val reihenfolge: Int?
)

// Spezifika DTOs

// DressurPruefungSpezifika DTOs
@Serializable
data class DressurPruefungSpezifikaDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val pruefungId: Uuid,
    val aufgabenbezeichnung: String?,
    val aufgabenbeschreibung: String?,
    val bewertungsschema: String?,
    val maxPunkte: Int?,
    val zeitlimit: Int?,
    val besonderheiten: String?
)
