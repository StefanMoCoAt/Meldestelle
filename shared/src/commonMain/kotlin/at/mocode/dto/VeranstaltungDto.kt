package at.mocode.dto

import at.mocode.enums.VeranstalterTypE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.KotlinLocalDateSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class VeranstaltungDto(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    val name: String,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumBis: LocalDate,
    val veranstalterName: String,
    val veranstalterOepsNummer: String?,
    val veranstalterTypE: VeranstalterTypE,
    val veranstaltungsortName: String,
    val veranstaltungsortAdresse: String,
    val kontaktpersonName: String?,
    val kontaktTelefon: String?,
    val kontaktEmail: String?,
    val webseite: String?,
    val logoUrl: String?,
    val anfahrtsplanInfo: String?,
    val sponsorInfos: List<String>,
    val dsgvoText: String?,
    val haftungsText: String?,
    val sonstigeBesondereBestimmungen: String?,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant,
    @Serializable(with = KotlinInstantSerializer::class)
    val updatedAt: Instant
)

@Serializable
data class CreateVeranstaltungDto(
    val name: String,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumBis: LocalDate,
    val veranstalterName: String,
    val veranstalterOepsNummer: String? = null,
    val veranstalterTypE: VeranstalterTypE = VeranstalterTypE.UNBEKANNT,
    val veranstaltungsortName: String,
    val veranstaltungsortAdresse: String,
    val kontaktpersonName: String? = null,
    val kontaktTelefon: String? = null,
    val kontaktEmail: String? = null,
    val webseite: String? = null,
    val logoUrl: String? = null,
    val anfahrtsplanInfo: String? = null,
    val sponsorInfos: List<String> = emptyList(),
    val dsgvoText: String? = null,
    val haftungsText: String? = null,
    val sonstigeBesondereBestimmungen: String? = null
)

@Serializable
data class UpdateVeranstaltungDto(
    val name: String,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    val datumBis: LocalDate,
    val veranstalterName: String,
    val veranstalterOepsNummer: String? = null,
    val veranstalterTypE: VeranstalterTypE = VeranstalterTypE.UNBEKANNT,
    val veranstaltungsortName: String,
    val veranstaltungsortAdresse: String,
    val kontaktpersonName: String? = null,
    val kontaktTelefon: String? = null,
    val kontaktEmail: String? = null,
    val webseite: String? = null,
    val logoUrl: String? = null,
    val anfahrtsplanInfo: String? = null,
    val sponsorInfos: List<String> = emptyList(),
    val dsgvoText: String? = null,
    val haftungsText: String? = null,
    val sonstigeBesondereBestimmungen: String? = null
)
