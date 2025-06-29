package at.mocode.shared.stammdaten

import at.mocode.shared.enums.LizenzTypE
import at.mocode.shared.enums.SparteE
import at.mocode.shared.serializers.KotlinLocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class LizenzInfo(
    val lizenzTyp: LizenzTypE,
    val stufe: String?,
    val sparteE: SparteE?,
    val gueltigBisJahr: Int?, // Jahr als Int
    @Serializable(with = KotlinLocalDateSerializer::class)
    val ausgestelltAm: LocalDate?
)
