package at.mocode.stammdaten

import at.mocode.enums.LizenzTypE
import at.mocode.enums.SparteE
import at.mocode.serializers.KotlinLocalDateSerializer
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
