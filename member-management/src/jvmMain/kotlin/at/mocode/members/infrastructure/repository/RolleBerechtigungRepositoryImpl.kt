package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.model.DomRolleBerechtigung
import at.mocode.members.domain.repository.RolleBerechtigungRepository
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock

/**
 * In-memory implementation of RolleBerechtigungRepository for testing and development.
 *
 * This implementation provides basic functionality without database persistence.
 * Replace with proper database implementation for production use.
 */
class RolleBerechtigungRepositoryImpl : RolleBerechtigungRepository {

    private val rolePermissions = mutableMapOf<Uuid, DomRolleBerechtigung>()

    override suspend fun save(rolleBerechtigung: DomRolleBerechtigung): DomRolleBerechtigung {
        val now = Clock.System.now()
        val updatedRolleBerechtigung = rolleBerechtigung.copy(updatedAt = now)
        rolePermissions[updatedRolleBerechtigung.rolleBerechtigungId] = updatedRolleBerechtigung
        return updatedRolleBerechtigung
    }

    override suspend fun findById(rolleBerechtigungId: Uuid): DomRolleBerechtigung? {
        return rolePermissions[rolleBerechtigungId]
    }

    override suspend fun findByRolleId(rolleId: Uuid, nurAktive: Boolean): List<DomRolleBerechtigung> {
        return rolePermissions.values.filter { rolleBerechtigung ->
            rolleBerechtigung.rolleId == rolleId && (!nurAktive || rolleBerechtigung.istAktiv)
        }
    }

    override suspend fun findByBerechtigungId(berechtigungId: Uuid, nurAktive: Boolean): List<DomRolleBerechtigung> {
        return rolePermissions.values.filter { rolleBerechtigung ->
            rolleBerechtigung.berechtigungId == berechtigungId && (!nurAktive || rolleBerechtigung.istAktiv)
        }
    }

    override suspend fun findByRolleAndBerechtigung(rolleId: Uuid, berechtigungId: Uuid): DomRolleBerechtigung? {
        return rolePermissions.values.find { rolleBerechtigung ->
            rolleBerechtigung.rolleId == rolleId && rolleBerechtigung.berechtigungId == berechtigungId
        }
    }

    override suspend fun findAllActive(): List<DomRolleBerechtigung> {
        return rolePermissions.values.filter { it.istAktiv }
    }

    override suspend fun findAll(): List<DomRolleBerechtigung> {
        return rolePermissions.values.toList()
    }

    override suspend fun deactivateRolleBerechtigung(rolleBerechtigungId: Uuid): Boolean {
        val rolleBerechtigung = rolePermissions[rolleBerechtigungId] ?: return false
        rolePermissions[rolleBerechtigungId] = rolleBerechtigung.copy(istAktiv = false, updatedAt = Clock.System.now())
        return true
    }

    override suspend fun deleteRolleBerechtigung(rolleBerechtigungId: Uuid): Boolean {
        return rolePermissions.remove(rolleBerechtigungId) != null
    }

    override suspend fun hasRolleBerechtigung(rolleId: Uuid, berechtigungId: Uuid): Boolean {
        return rolePermissions.values.any { rolleBerechtigung ->
            rolleBerechtigung.rolleId == rolleId &&
            rolleBerechtigung.berechtigungId == berechtigungId &&
            rolleBerechtigung.istAktiv
        }
    }

    override suspend fun assignBerechtigungToRolle(rolleId: Uuid, berechtigungId: Uuid, zugewiesenVon: Uuid?): DomRolleBerechtigung {
        // Check if assignment already exists
        val existing = findByRolleAndBerechtigung(rolleId, berechtigungId)
        if (existing != null) {
            // If it exists but is inactive, reactivate it
            if (!existing.istAktiv) {
                val reactivated = existing.copy(istAktiv = true, updatedAt = Clock.System.now())
                return save(reactivated)
            }
            return existing
        }

        // Create new assignment
        val newAssignment = DomRolleBerechtigung(
            rolleId = rolleId,
            berechtigungId = berechtigungId,
            zugewiesenVon = zugewiesenVon
        )
        return save(newAssignment)
    }

    override suspend fun revokeBerechtigungFromRolle(rolleId: Uuid, berechtigungId: Uuid): Boolean {
        val rolleBerechtigung = findByRolleAndBerechtigung(rolleId, berechtigungId) ?: return false
        return deactivateRolleBerechtigung(rolleBerechtigung.rolleBerechtigungId)
    }
}
