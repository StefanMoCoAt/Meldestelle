package at.mocode.shared.model

import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class MeisterschaftReferenz(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    var meisterschaftId: Uuid, // FK zu einer Meisterschafts-Entität
    var name: String,
    var betrifftBewerbNummern: List<Int>,
    var berechnungsstrategie: String?,
    var reglementUrl: String?
)

@Serializable
data class CupReferenz(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    var cupId: Uuid, // FK zu einer Meisterschafts-Entität
    var name: String,
    var betrifftBewerbNummern: List<Int>,
    var berechnungsstrategie: String?,
    var reglementUrl: String?
)

@Serializable
data class SonderpruefungReferenz(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    var cupId: Uuid, // FK zu einer Meisterschafts-Entität
    var name: String,
    var betrifftBewerbNummern: List<Int>,
    var berechnungsstrategie: String?,
    var reglementUrl: String?
)
