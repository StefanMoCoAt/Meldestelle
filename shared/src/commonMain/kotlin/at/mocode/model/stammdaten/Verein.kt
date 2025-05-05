package at.mocode.model.stammdaten

import at.mocode.model.serializer.JavaUUIDSerializer
import at.mocode.model.serializer.KotlinInstantSerializer
import com.benasher44.uuid.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Verein(
    @Serializable(with = JavaUUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
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
