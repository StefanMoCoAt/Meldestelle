package at.mocode.shared.model.entitaeten

import at.mocode.shared.model.serializers.KotlinInstantSerializer
import at.mocode.shared.model.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class Abteilung(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid,
    @Serializable(with = UuidSerializer::class)
    val bewerbId: Uuid,
    val bezeichnung: String, // z.B. "R1", "R2/RS2 u. höher"
    val beginnZeit: String, // TIME als String, z.B. "09:00" oder "anschließend"
    val istFixeBeginnZeit: Boolean = false,
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)

