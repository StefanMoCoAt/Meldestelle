package at.mocode.horses.service.config

import at.mocode.horses.application.usecase.CreateHorseUseCase
import at.mocode.horses.application.usecase.TransactionalCreateHorseUseCase
import at.mocode.horses.application.usecase.UpdateHorseUseCase
import at.mocode.horses.application.usecase.DeleteHorseUseCase
import at.mocode.horses.application.usecase.GetHorseUseCase
import at.mocode.horses.domain.repository.HorseRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Application configuration for the Horses Service.
 *
 * This configuration wires the use cases as Spring beans.
 */
@Configuration
class ApplicationConfiguration {

    /**
     * Creates the CreateHorseUseCase as a Spring bean.
     */
    @Bean
    fun createHorseUseCase(horseRepository: HorseRepository): CreateHorseUseCase {
        return CreateHorseUseCase(horseRepository)
    }

    /**
     * Creates the TransactionalCreateHorseUseCase as a Spring bean.
     * This version ensures all database operations run within a single transaction.
     */
    @Bean
    fun transactionalCreateHorseUseCase(horseRepository: HorseRepository): TransactionalCreateHorseUseCase {
        return TransactionalCreateHorseUseCase(horseRepository)
    }

    /**
     * Creates the UpdateHorseUseCase as a Spring bean.
     */
    @Bean
    fun updateHorseUseCase(horseRepository: HorseRepository): UpdateHorseUseCase {
        return UpdateHorseUseCase(horseRepository)
    }

    /**
     * Creates the DeleteHorseUseCase as a Spring bean.
     */
    @Bean
    fun deleteHorseUseCase(horseRepository: HorseRepository): DeleteHorseUseCase {
        return DeleteHorseUseCase(horseRepository)
    }

    /**
     * Creates the GetHorseUseCase as a Spring bean.
     */
    @Bean
    fun getHorseUseCase(horseRepository: HorseRepository): GetHorseUseCase {
        return GetHorseUseCase(horseRepository)
    }
}
