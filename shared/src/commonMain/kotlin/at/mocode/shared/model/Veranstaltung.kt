package at.mocode.shared.model

import at.mocode.shared.enums.VeranstalterTypE
import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.KotlinLocalDateSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Veranstaltung(
    @Serializable(with = UuidSerializer::class) // Beispiel für Serializer, falls nötig
    val id: Uuid = uuid4(),
    var name: String,
    @Serializable(with = KotlinLocalDateSerializer::class) // Beispiel für Serializer
    var datumVon: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var datumBis: LocalDate,
    var veranstalterName: String,
    var veranstalterOepsNummer: String?,
    var veranstalterTypE: VeranstalterTypE = VeranstalterTypE.UNBEKANNT,
    var veranstaltungsortName: String,
    var veranstaltungsortAdresse: String,
    var kontaktpersonName: String?,
    var kontaktTelefon: String?,
    var kontaktEmail: String?,
    var webseite: String?,
    var logoUrl: String?,
    var anfahrtsplanInfo: String?,
    var sponsorInfos: List<String> = emptyList(),
    var dsgvoText: String?,
    var haftungsText: String?,
    var sonstigeBesondereBestimmungen: String?,
    @Serializable(with = KotlinInstantSerializer::class) // Beispiel für Serializer
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
