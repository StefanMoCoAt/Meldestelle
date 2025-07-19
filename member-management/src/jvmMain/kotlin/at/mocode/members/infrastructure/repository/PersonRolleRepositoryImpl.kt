package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.model.DomPersonRolle
import at.mocode.members.domain.repository.PersonRolleRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * In-memory implementation of PersonRolleRepository for testing and development.
 *
 * This implementation provides basic functionality without database persistence.
 * Replace with proper database implementation for production use.
 */
class PersonRolleRepositoryImpl : PersonRolleRepository {

    private val personRoles = mutableMapOf<Uuid, DomPersonRolle>()

    override suspend fun save(personRolle: DomPersonRolle): DomPersonRolle {
        val now = Clock.System.now()
        val updatedPersonRolle = personRolle.copy(updatedAt = now)
        personRoles[updatedPersonRolle.personRolleId] = updatedPersonRolle
        return updatedPersonRolle
    }

    override suspend fun findById(personRolleId: Uuid): DomPersonRolle? {
        return personRoles[personRolleId]
    }

    override suspend fun findByPersonId(personId: Uuid, nurAktive: Boolean): List<DomPersonRolle> {
        return personRoles.values.filter { personRolle ->
            personRolle.personId == personId && (!nurAktive || personRolle.istAktiv)
        }
    }

    override suspend fun findByRolleId(rolleId: Uuid, nurAktive: Boolean): List<DomPersonRolle> {
        return personRoles.values.filter { personRolle ->
            personRolle.rolleId == rolleId && (!nurAktive || personRolle.istAktiv)
        }
    }

    override suspend fun findByVereinId(vereinId: Uuid, nurAktive: Boolean): List<DomPersonRolle> {
        return personRoles.values.filter { personRolle ->
            personRolle.vereinId == vereinId && (!nurAktive || personRolle.istAktiv)
        }
    }

    override suspend fun findByPersonAndRolle(personId: Uuid, rolleId: Uuid, vereinId: Uuid?): DomPersonRolle? {
        return personRoles.values.find { personRolle ->
            personRolle.personId == personId &&
            personRolle.rolleId == rolleId &&
            (vereinId == null || personRolle.vereinId == vereinId)
        }
    }

    override suspend fun findValidAt(stichtag: LocalDate, nurAktive: Boolean): List<DomPersonRolle> {
        return personRoles.values.filter { personRolle ->
            val isValid = personRolle.gueltigVon <= stichtag &&
                         (personRolle.gueltigBis == null || personRolle.gueltigBis!! >= stichtag)
            isValid && (!nurAktive || personRolle.istAktiv)
        }
    }

    override suspend fun findByPersonValidAt(personId: Uuid, stichtag: LocalDate, nurAktive: Boolean): List<DomPersonRolle> {
        return personRoles.values.filter { personRolle ->
            val isValid = personRolle.personId == personId &&
                         personRolle.gueltigVon <= stichtag &&
                         (personRolle.gueltigBis == null || personRolle.gueltigBis!! >= stichtag)
            isValid && (!nurAktive || personRolle.istAktiv)
        }
    }

    override suspend fun deactivatePersonRolle(personRolleId: Uuid): Boolean {
        val personRolle = personRoles[personRolleId] ?: return false
        personRoles[personRolleId] = personRolle.copy(istAktiv = false, updatedAt = Clock.System.now())
        return true
    }

    override suspend fun deletePersonRolle(personRolleId: Uuid): Boolean {
        return personRoles.remove(personRolleId) != null
    }

    override suspend fun hasPersonRolle(personId: Uuid, rolleId: Uuid, vereinId: Uuid?, stichtag: LocalDate?): Boolean {
        val checkDate = stichtag ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

        return personRoles.values.any { personRolle ->
            personRolle.personId == personId &&
            personRolle.rolleId == rolleId &&
            (vereinId == null || personRolle.vereinId == vereinId) &&
            personRolle.istAktiv &&
            personRolle.gueltigVon <= checkDate &&
            (personRolle.gueltigBis == null || personRolle.gueltigBis!! >= checkDate)
        }
    }
}
