package at.mocode.di

import at.mocode.members.application.usecase.CreatePersonUseCase
import at.mocode.members.domain.repository.PersonRepository
import at.mocode.members.domain.repository.VereinRepository
import at.mocode.members.domain.service.MasterDataService
import at.mocode.ui.viewmodel.CreatePersonViewModel
import at.mocode.ui.viewmodel.PersonListViewModel

/**
 * Simple dependency injection container for the application.
 * In a real application, you might want to use a proper DI framework like Koin.
 */
object AppDependencies {

    // Mock implementations for demonstration
    // In a real application, these would be proper implementations
    private val mockPersonRepository = object : PersonRepository {
        override suspend fun save(person: at.mocode.members.domain.model.DomPerson): at.mocode.members.domain.model.DomPerson {
            // Mock implementation - just return the person with an ID
            return person.copy(id = com.benasher44.uuid.uuid4())
        }

        override suspend fun findById(id: com.benasher44.uuid.Uuid): at.mocode.members.domain.model.DomPerson? {
            return null // Mock implementation
        }

        override suspend fun findByOepsSatzNr(oepsSatzNr: String): at.mocode.members.domain.model.DomPerson? {
            return null // Mock implementation
        }

        override suspend fun existsByOepsSatzNr(oepsSatzNr: String): Boolean {
            return false // Mock implementation - no duplicates for demo
        }

        override suspend fun findAll(): List<at.mocode.members.domain.model.DomPerson> {
            return emptyList() // Mock implementation
        }

        override suspend fun delete(id: com.benasher44.uuid.Uuid) {
            // Mock implementation
        }
    }

    private val mockVereinRepository = object : VereinRepository {
        override suspend fun findById(id: com.benasher44.uuid.Uuid): at.mocode.members.domain.model.DomVerein? {
            return null // Mock implementation
        }

        override suspend fun existsById(id: com.benasher44.uuid.Uuid): Boolean {
            return true // Mock implementation - assume all clubs exist
        }

        override suspend fun findAll(): List<at.mocode.members.domain.model.DomVerein> {
            return emptyList() // Mock implementation
        }
    }

    private val mockMasterDataService = object : MasterDataService {
        override suspend fun countryExists(countryId: com.benasher44.uuid.Uuid): Boolean {
            return true // Mock implementation - assume all countries exist
        }
    }

    // Use case instances
    private val createPersonUseCase = CreatePersonUseCase(
        personRepository = mockPersonRepository,
        vereinRepository = mockVereinRepository,
        masterDataService = mockMasterDataService
    )

    // ViewModel factory methods
    fun createPersonViewModel(): CreatePersonViewModel {
        return CreatePersonViewModel(createPersonUseCase)
    }

    fun personListViewModel(): PersonListViewModel {
        return PersonListViewModel(mockPersonRepository)
    }
}
