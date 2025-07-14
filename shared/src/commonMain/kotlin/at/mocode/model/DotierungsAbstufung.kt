package at.mocode.model

import at.mocode.serializers.BigDecimalSerializer
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
data class DotierungsAbstufung(
    val platz: Int,         // Für welchen Platz gilt dieser Geldpreis (z.B. 1, 2, 3)
    @Serializable(with = BigDecimalSerializer::class)
    val betrag: BigDecimal,
    val beschreibung: String? = null // z.B. "Ehrenpreis zusätzlich"
)
