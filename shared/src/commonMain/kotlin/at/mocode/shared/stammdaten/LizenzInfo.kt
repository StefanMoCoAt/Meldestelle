package at.mocode.shared.stammdaten

import at.mocode.shared.enums.LizenzTyp
import at.mocode.shared.enums.Sparte
import at.mocode.shared.serializers.KotlinLocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class LizenzInfo(
    val lizenzTyp: LizenzTyp,
    val stufe: String?,
    val sparte: Sparte?,
    val gueltigBisJahr: Int?, // Jahr als Int
    @Serializable(with = KotlinLocalDateSerializer::class)
    val ausgestelltAm: LocalDate?
)
