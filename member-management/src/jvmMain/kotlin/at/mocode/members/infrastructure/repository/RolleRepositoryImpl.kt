package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.model.DomRolle
import at.mocode.members.domain.repository.RolleRepository
import at.mocode.enums.RolleE
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock

/**
 * In-memory implementation of RolleRepository for testing and development.
 *
 * This implementation provides basic functionality without database persistence.
 * Replace with proper database implementation for production use.
 */
class RolleRepositoryImpl : RolleRepository {

    private val roles = mutableMapOf<Uuid, DomRolle>()

    init {
        // Initialize with default roles
        val defaultRoles = listOf(
            DomRolle(
                rolleId = uuid4(),
                rolleTyp = RolleE.ADMIN,
                name = "Administrator",
                beschreibung = "System administrator with full access",
                istAktiv = true,
                istSystemRolle = true
            ),
            DomRolle(
                rolleId = uuid4(),
                rolleTyp = RolleE.VEREINS_ADMIN,
                name = "Vereins Administrator",
                beschreibung = "Club administrator",
                istAktiv = true,
                istSystemRolle = true
            ),
            DomRolle(
                rolleId = uuid4(),
                rolleTyp = RolleE.REITER,
                name = "Reiter",
                beschreibung = "Rider",
                istAktiv = true,
                istSystemRolle = true
            )
        )

        defaultRoles.forEach { role ->
            roles[role.rolleId!!] = role
        }
    }

    override suspend fun save(rolle: DomRolle): DomRolle {
        val now = Clock.System.now()
        val updatedRolle = rolle.copy(updatedAt = now)
        roles[updatedRolle.rolleId!!] = updatedRolle
        return updatedRolle
    }

    override suspend fun findById(rolleId: Uuid): DomRolle? {
        return roles[rolleId]
    }

    override suspend fun findByTyp(rolleTyp: RolleE): DomRolle? {
        return roles.values.find { it.rolleTyp == rolleTyp }
    }

    override suspend fun findByName(name: String): List<DomRolle> {
        return roles.values.filter { it.name.contains(name, ignoreCase = true) }
    }

    override suspend fun findAllActive(): List<DomRolle> {
        return roles.values.filter { it.istAktiv }
    }

    override suspend fun findAll(): List<DomRolle> {
        return roles.values.toList()
    }

    override suspend fun deactivateRolle(rolleId: Uuid): Boolean {
        val rolle = roles[rolleId] ?: return false
        roles[rolleId] = rolle.copy(istAktiv = false, updatedAt = Clock.System.now())
        return true
    }

    override suspend fun deleteRolle(rolleId: Uuid): Boolean {
        val rolle = roles[rolleId] ?: return false
        // Don't allow deletion of system roles
        if (rolle.istSystemRolle) return false
        roles.remove(rolleId)
        return true
    }

    override suspend fun existsByTyp(rolleTyp: RolleE): Boolean {
        return roles.values.any { it.rolleTyp == rolleTyp }
    }

}
