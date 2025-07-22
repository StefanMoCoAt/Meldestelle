package at.mocode.client.web.di

import at.mocode.client.common.api.ApiClient
import at.mocode.client.common.repository.ClientEventRepository
import at.mocode.client.common.repository.ClientPersonRepository
import at.mocode.client.common.repository.EventRepository
import at.mocode.client.common.repository.PersonRepository
import at.mocode.client.web.viewmodel.CreatePersonViewModel
import at.mocode.client.web.viewmodel.PersonListViewModel

/**
 * Simple dependency injection container for the application.
 * In a real application, you might want to use a proper DI framework like Koin.
 */
object AppDependencies {

    // Repository instances
    private val personRepository: PersonRepository by lazy { ClientPersonRepository() }
    private val eventRepository: EventRepository by lazy { ClientEventRepository() }

    // ViewModel factory methods
    fun createPersonViewModel(): CreatePersonViewModel {
        return CreatePersonViewModel(personRepository)
    }

    fun personListViewModel(): PersonListViewModel {
        return PersonListViewModel(personRepository)
    }

    // Helper method to initialize dependencies
    fun initialize() {
        // Initialize ApiClient if needed
        println("AppDependencies initialized")
    }
}
