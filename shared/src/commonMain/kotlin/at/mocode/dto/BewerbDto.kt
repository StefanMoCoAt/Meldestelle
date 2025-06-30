package at.mocode.dto

import at.mocode.enums.BeginnzeitTypE
import at.mocode.enums.SparteE
import at.mocode.model.DotierungsAbstufung
import at.mocode.serializers.*
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class BewerbDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val turnierId: Uuid,
    val nummer: String,
    val bezeichnungOffiziell: String,
    val internerName: String?,
    val sparteE: SparteE,
    val klasse: String?,
    val kategorieOetoDesBewerbs: String?,
    val teilnahmebedingungenText: String?,
    val maxPferdeProReiter: Int?,
    val pferdealterAnforderung: String?,
    val zusatzTextZeile1: String?,
    val zusatzTextZeile2: String?,
    val zusatzTextZeile3: String?,
    val logoBewerbUrl: String?,
    val parcoursskizzeUrl: String?,
    val pruefungsArtDetailName: String?,
    @Serializable(with = UuidSerializer::class)
    val pruefungsaufgabeId: Uuid?,
    @Serializable(with = UuidSerializer::class)
    val richtverfahrenId: Uuid?,
    val anzahlRichterGeplant: Int?,
    val paraGradeAnforderung: String?,
    val istManuellKalkuliert: Boolean,
    val istDotiert: Boolean,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeldStandard: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeldKaderreiter: BigDecimal?,
    val auszahlungsModusGeldpreis: String?,
    val hatGeldpreisFuerKaderreiter: Boolean,
    @Serializable(with = UuidSerializer::class)
    val geldpreisVorlageId: Uuid?,
    val dotierungenManuell: List<DotierungsAbstufung>,
    @Serializable(with = UuidSerializer::class)
    val standardPlatzId: Uuid?,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val standardDatum: LocalDate?,
    val standardBeginnzeitTypE: BeginnzeitTypE,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val standardBeginnzeitFix: LocalTime?,
    @Serializable(with = UuidSerializer::class)
    val standardBeginnNachBewerbId: Uuid?,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val standardBeginnzeitCa: LocalTime?,
    val standardDauerProStartGeschaetztSek: Int?,
    val standardUmbauzeitNachBewerbMin: Int?,
    val standardBesichtigungszeitVorBewerbMin: Int?,
    val standardStechzeitZusaetzlichMin: Int?,
    val oepsBewerbsartCodeZns: String?,
    val oepsAltersklasseCodeZns: String?,
    val oepsPferderassenCodeZns: String?,
    val notizenIntern: String?,
    val istStartlisteFinal: Boolean,
    val istErgebnislisteFinal: Boolean,
    val erfordertAbteilungsAuswahlFuerNennung: Boolean,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateBewerbDto(
    @Serializable(with = UuidSerializer::class)
    val turnierId: Uuid,
    val nummer: String,
    val bezeichnungOffiziell: String,
    val internerName: String? = null,
    val sparteE: SparteE,
    val klasse: String? = null,
    val kategorieOetoDesBewerbs: String? = null,
    val teilnahmebedingungenText: String? = null,
    val maxPferdeProReiter: Int? = null,
    val pferdealterAnforderung: String? = null,
    val zusatzTextZeile1: String? = null,
    val zusatzTextZeile2: String? = null,
    val zusatzTextZeile3: String? = null,
    val logoBewerbUrl: String? = null,
    val parcoursskizzeUrl: String? = null,
    val pruefungsArtDetailName: String? = null,
    @Serializable(with = UuidSerializer::class)
    val pruefungsaufgabeId: Uuid? = null,
    @Serializable(with = UuidSerializer::class)
    val richtverfahrenId: Uuid? = null,
    val anzahlRichterGeplant: Int? = 1,
    val paraGradeAnforderung: String? = null,
    val istManuellKalkuliert: Boolean = false,
    val istDotiert: Boolean = false,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeldStandard: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeldKaderreiter: BigDecimal? = null,
    val auszahlungsModusGeldpreis: String? = null,
    val hatGeldpreisFuerKaderreiter: Boolean = false,
    @Serializable(with = UuidSerializer::class)
    val geldpreisVorlageId: Uuid? = null,
    val dotierungenManuell: List<DotierungsAbstufung> = emptyList(),
    @Serializable(with = UuidSerializer::class)
    val standardPlatzId: Uuid? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val standardDatum: LocalDate? = null,
    val standardBeginnzeitTypE: BeginnzeitTypE = BeginnzeitTypE.ANSCHLIESSEND,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val standardBeginnzeitFix: LocalTime? = null,
    @Serializable(with = UuidSerializer::class)
    val standardBeginnNachBewerbId: Uuid? = null,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val standardBeginnzeitCa: LocalTime? = null,
    val standardDauerProStartGeschaetztSek: Int? = 120,
    val standardUmbauzeitNachBewerbMin: Int? = 10,
    val standardBesichtigungszeitVorBewerbMin: Int? = 10,
    val standardStechzeitZusaetzlichMin: Int? = 0,
    val oepsBewerbsartCodeZns: String? = null,
    val oepsAltersklasseCodeZns: String? = null,
    val oepsPferderassenCodeZns: String? = null,
    val notizenIntern: String? = null,
    val istStartlisteFinal: Boolean = false,
    val istErgebnislisteFinal: Boolean = false,
    val erfordertAbteilungsAuswahlFuerNennung: Boolean = true
)

@Serializable
data class UpdateBewerbDto(
    val nummer: String,
    val bezeichnungOffiziell: String,
    val internerName: String? = null,
    val sparteE: SparteE,
    val klasse: String? = null,
    val kategorieOetoDesBewerbs: String? = null,
    val teilnahmebedingungenText: String? = null,
    val maxPferdeProReiter: Int? = null,
    val pferdealterAnforderung: String? = null,
    val zusatzTextZeile1: String? = null,
    val zusatzTextZeile2: String? = null,
    val zusatzTextZeile3: String? = null,
    val logoBewerbUrl: String? = null,
    val parcoursskizzeUrl: String? = null,
    val pruefungsArtDetailName: String? = null,
    @Serializable(with = UuidSerializer::class)
    val pruefungsaufgabeId: Uuid? = null,
    @Serializable(with = UuidSerializer::class)
    val richtverfahrenId: Uuid? = null,
    val anzahlRichterGeplant: Int? = 1,
    val paraGradeAnforderung: String? = null,
    val istManuellKalkuliert: Boolean = false,
    val istDotiert: Boolean = false,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeldStandard: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val startgeldKaderreiter: BigDecimal? = null,
    val auszahlungsModusGeldpreis: String? = null,
    val hatGeldpreisFuerKaderreiter: Boolean = false,
    @Serializable(with = UuidSerializer::class)
    val geldpreisVorlageId: Uuid? = null,
    val dotierungenManuell: List<DotierungsAbstufung> = emptyList(),
    @Serializable(with = UuidSerializer::class)
    val standardPlatzId: Uuid? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val standardDatum: LocalDate? = null,
    val standardBeginnzeitTypE: BeginnzeitTypE = BeginnzeitTypE.ANSCHLIESSEND,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val standardBeginnzeitFix: LocalTime? = null,
    @Serializable(with = UuidSerializer::class)
    val standardBeginnNachBewerbId: Uuid? = null,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    val standardBeginnzeitCa: LocalTime? = null,
    val standardDauerProStartGeschaetztSek: Int? = 120,
    val standardUmbauzeitNachBewerbMin: Int? = 10,
    val standardBesichtigungszeitVorBewerbMin: Int? = 10,
    val standardStechzeitZusaetzlichMin: Int? = 0,
    val oepsBewerbsartCodeZns: String? = null,
    val oepsAltersklasseCodeZns: String? = null,
    val oepsPferderassenCodeZns: String? = null,
    val notizenIntern: String? = null,
    val istStartlisteFinal: Boolean = false,
    val istErgebnislisteFinal: Boolean = false,
    val erfordertAbteilungsAuswahlFuerNennung: Boolean = true
)
