package at.mocode.model.entitaeten

import at.mocode.model.serializers.UuidSerializer
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
