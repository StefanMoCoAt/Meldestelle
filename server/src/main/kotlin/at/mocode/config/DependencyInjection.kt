package at.mocode.config

import at.mocode.email.EmailService
import at.mocode.repository.NennungRepository
import at.mocode.repository.TurnierRepository
import at.mocode.views.HomeView
import at.mocode.views.NennungView
import org.slf4j.LoggerFactory

/**
 * Simple dependency injection container for the application.
 * This singleton provides access to all services and repositories.
 */
object DependencyInjection {
    private val log = LoggerFactory.getLogger(DependencyInjection::class.java)

    // Repositories
    val turnierRepository by lazy {
        log.debug("Creating TurnierRepository")
        TurnierRepository()
    }

    val nennungRepository by lazy {
        log.debug("Creating NennungRepository")
        NennungRepository()
    }

    // Services
    val emailService by lazy {
        log.debug("Creating EmailService")
        EmailService.getInstance()
    }

    // Views
    val homeView by lazy {
        log.debug("Creating HomeView")
        HomeView()
    }

    val nennungView by lazy {
        log.debug("Creating NennungView")
        NennungView()
    }
}
