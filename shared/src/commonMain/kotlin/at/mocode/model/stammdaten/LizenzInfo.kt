package at.mocode.model.stammdaten

import at.mocode.model.enums.LizenzTyp
import at.mocode.model.enums.Sparte
import at.mocode.model.serializer.KotlinLocalDateSerializer
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
