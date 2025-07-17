package at.mocode.masterdata.domain.model

import at.mocode.enums.PlatzTypE
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Platz(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    var turnierId: Uuid,
    var name: String,
    var dimension: String?,
    var boden: String?,
    var typ: PlatzTypE
)
