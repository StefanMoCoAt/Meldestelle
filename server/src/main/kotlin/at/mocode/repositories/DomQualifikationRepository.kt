package at.mocode.repositories

import at.mocode.model.domaene.DomQualifikation
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate

interface DomQualifikationRepository {
    suspend fun findAll(): List<DomQualifikation>
    suspend fun findById(id: Uuid): DomQualifikation?
    suspend fun findByPersonId(personId: Uuid): List<DomQualifikation>
    suspend fun findByQualTypId(qualTypId: Uuid): List<DomQualifikation>
    suspend fun findActiveByPersonId(personId: Uuid): List<DomQualifikation>
    suspend fun findByValidityPeriod(fromDate: LocalDate?, toDate: LocalDate?): List<DomQualifikation>
    suspend fun create(domQualifikation: DomQualifikation): DomQualifikation
    suspend fun update(id: Uuid, domQualifikation: DomQualifikation): DomQualifikation?
    suspend fun delete(id: Uuid): Boolean
    suspend fun search(query: String): List<DomQualifikation>
}
