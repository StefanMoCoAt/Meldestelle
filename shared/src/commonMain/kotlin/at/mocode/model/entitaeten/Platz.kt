package at.mocode.model.entitaeten

import at.mocode.model.enums.PlatzTyp
import at.mocode.model.serializers.UuidSerializer
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
    var typ: PlatzTyp
)
