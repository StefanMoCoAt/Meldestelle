package at.mocode.shared.model

import at.mocode.shared.enums.PlatzTypE
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Platz(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    var name: String,
    var dimension: String?,
    var boden: String?,
    var typ: PlatzTypE
)
