package at.mocode.dto

import at.mocode.enums.BeginnzeitTypE
import at.mocode.model.DotierungsAbstufung
import at.mocode.serializers.*
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class AbteilungDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val bewerbId: Uuid,
    val abteilungsKennzeichen: String,
    val bezeichnungIntern: String?,
    val bezeichnungAufStartliste: String?,
    val teilungsKriteriumLizenz: String?,
    val teilungsKriteriumPferdealter: String?,
    val teilungsKriteriumAltersklasseReiter: String?,
    val teilungsKriteriumAnzahlMin: Int?,
    val teilungsKriteriumAnzahlMax: Int?,
    val teilungsKriteriumFreiText: String?,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeld: BigDecimal?,
    val dotierungen: List<DotierungsAbstufung>,
    @Serializable(with = UuidSerializer::class)
    val platzId: Uuid?,
    val datum: LocalDate?,
    val beginnzeitTypE: BeginnzeitTypE,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val beginnzeitFix: LocalTime?,
    @Serializable(with = UuidSerializer::class)
    val beginnNachAbteilungId: Uuid?,
    val beginnzeitCa: LocalTime?,
    val dauerProStartGeschaetztSek: Int?,
    val umbauzeitNachAbteilungMin: Int?,
    val besichtigungszeitVorAbteilungMin: Int?,
    val stechzeitZusaetzlichMin: Int?,
    val anzahlStarter: Int,
    val istAktiv: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateAbteilungDto(
    @Serializable(with = UuidSerializer::class)
    val bewerbId: Uuid,
    val abteilungsKennzeichen: String,
    val bezeichnungIntern: String? = null,
    val bezeichnungAufStartliste: String? = null,
    val teilungsKriteriumLizenz: String? = null,
    val teilungsKriteriumPferdealter: String? = null,
    val teilungsKriteriumAltersklasseReiter: String? = null,
    val teilungsKriteriumAnzahlMin: Int? = null,
    val teilungsKriteriumAnzahlMax: Int? = null,
    val teilungsKriteriumFreiText: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeld: BigDecimal? = null,
    val dotierungen: List<DotierungsAbstufung> = emptyList(),
    @Serializable(with = UuidSerializer::class)
    val platzId: Uuid? = null,
    val datum: LocalDate? = null,
    val beginnzeitTypE: BeginnzeitTypE = BeginnzeitTypE.ANSCHLIESSEND,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val beginnzeitFix: LocalTime? = null,
    @Serializable(with = UuidSerializer::class)
    val beginnNachAbteilungId: Uuid? = null,
    val beginnzeitCa: LocalTime? = null,
    val dauerProStartGeschaetztSek: Int? = null,
    val umbauzeitNachAbteilungMin: Int? = null,
    val besichtigungszeitVorAbteilungMin: Int? = null,
    val stechzeitZusaetzlichMin: Int? = null,
    val anzahlStarter: Int = 0,
    val istAktiv: Boolean = true
)

@Serializable
data class UpdateAbteilungDto(
    val abteilungsKennzeichen: String,
    val bezeichnungIntern: String? = null,
    val bezeichnungAufStartliste: String? = null,
    val teilungsKriteriumLizenz: String? = null,
    val teilungsKriteriumPferdealter: String? = null,
    val teilungsKriteriumAltersklasseReiter: String? = null,
    val teilungsKriteriumAnzahlMin: Int? = null,
    val teilungsKriteriumAnzahlMax: Int? = null,
    val teilungsKriteriumFreiText: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeld: BigDecimal? = null,
    val dotierungen: List<DotierungsAbstufung> = emptyList(),
    @Serializable(with = UuidSerializer::class)
    val platzId: Uuid? = null,
    val datum: LocalDate? = null,
    val beginnzeitTypE: BeginnzeitTypE = BeginnzeitTypE.ANSCHLIESSEND,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val beginnzeitFix: LocalTime? = null,
    @Serializable(with = UuidSerializer::class)
    val beginnNachAbteilungId: Uuid? = null,
    val beginnzeitCa: LocalTime? = null,
    val dauerProStartGeschaetztSek: Int? = null,
    val umbauzeitNachAbteilungMin: Int? = null,
    val besichtigungszeitVorAbteilungMin: Int? = null,
    val stechzeitZusaetzlichMin: Int? = null,
    val anzahlStarter: Int = 0,
    val istAktiv: Boolean = true
)
