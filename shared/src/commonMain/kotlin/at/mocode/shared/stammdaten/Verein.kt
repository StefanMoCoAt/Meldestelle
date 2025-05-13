package at.mocode.shared.stammdaten

import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Verein(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    var oepsVereinsNr: String,
    var name: String,
    var kuerzel: String?,
    var bundesland: String?,
    var adresse: String?,
    var plz: String?,
    var ort: String?,
    var email: String?,
    var telefon: String?,
    var webseite: String?,
    var istAktiv: Boolean = true,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
