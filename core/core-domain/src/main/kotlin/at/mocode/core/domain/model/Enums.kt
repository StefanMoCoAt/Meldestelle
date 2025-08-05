package at.mocode.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Defines the source of a data record. This is a cross-cutting concern
 * and therefore part of the Shared Kernel.
 */
@Serializable
enum class DatenQuelleE {
    MANUELL,
    IMPORT_ZNS,
    SYSTEM_GENERATED,
    IMPORT_API
}
