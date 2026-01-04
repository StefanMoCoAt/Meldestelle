package at.mocode.ping.infrastructure.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * JPA Entity (Infrastructure Detail).
 * Spiegelt die Datenbank-Tabelle wider.
 * Nutzt java.util.UUID für JPA-Kompatibilität (bis Hibernate kotlin.uuid nativ unterstützt).
 */
@Entity
@Table(name = "pings")
class PingJpaEntity(
    @Id
    val id: UUID,
    val message: String,
    val timestamp: Instant
) {
    // Default constructor for JPA
    protected constructor() : this(UUID.randomUUID(), "", Instant.now())
}
