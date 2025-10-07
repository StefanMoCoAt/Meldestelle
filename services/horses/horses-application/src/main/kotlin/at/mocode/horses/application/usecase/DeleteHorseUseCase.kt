@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.horses.application.usecase

import at.mocode.horses.domain.repository.HorseRepository
import kotlin.uuid.Uuid

/**
 * Use case for deleting a horse from the registry.
 *
 * This use case handles the business logic for horse deletion including
 * existence checks and business rule validation.
 */
class DeleteHorseUseCase(
    private val horseRepository: HorseRepository
) {

    /**
     * Request data for deleting a horse.
     */
    data class DeleteHorseRequest(
        val pferdId: Uuid,
        val forceDelete: Boolean = false
    )

    /**
     * Response data for horse deletion.
     */
    data class DeleteHorseResponse(
        val success: Boolean,
        val errors: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    )

    /**
     * Executes the horse deletion use case.
     *
     * @param request The horse deletion request data
     * @return DeleteHorseResponse indicating success or failure with errors
     */
    suspend fun execute(request: DeleteHorseRequest): DeleteHorseResponse {
        // Check if horse exists
        val existingHorse = horseRepository.findById(request.pferdId)
            ?: return DeleteHorseResponse(
                success = false,
                errors = listOf("Horse not found")
            )

        // Check business rules for deletion
        val businessRuleErrors = checkBusinessRules(request, existingHorse.pferdeName)
        if (businessRuleErrors.isNotEmpty() && !request.forceDelete) {
            return DeleteHorseResponse(
                success = false,
                errors = businessRuleErrors
            )
        }

        // Generate warnings for important information
        val warnings = generateWarnings(existingHorse.pferdeName, existingHorse.oepsNummer, existingHorse.feiNummer)

        // Perform the deletion
        val deleted = horseRepository.delete(request.pferdId)

        return if (deleted) {
            DeleteHorseResponse(
                success = true,
                warnings = warnings
            )
        } else {
            DeleteHorseResponse(
                success = false,
                errors = listOf("Failed to delete horse from database")
            )
        }
    }

    /**
     * Soft delete alternative - marks horse as inactive instead of deleting.
     */
    suspend fun softDelete(pferdId: Uuid): DeleteHorseResponse {
        val existingHorse = horseRepository.findById(pferdId)
            ?: return DeleteHorseResponse(
                success = false,
                errors = listOf("Horse not found")
            )

        if (!existingHorse.istAktiv) {
            return DeleteHorseResponse(
                success = false,
                errors = listOf("Horse is already inactive")
            )
        }

        // Mark as inactive
        val inactiveHorse = existingHorse.copy(istAktiv = false).withUpdatedTimestamp()
        horseRepository.save(inactiveHorse)

        return DeleteHorseResponse(
            success = true,
            warnings = listOf("Horse marked as inactive instead of deleted")
        )
    }

    /**
     * Checks business rules that might prevent deletion.
     */
    private suspend fun checkBusinessRules(request: DeleteHorseRequest, horseName: String): List<String> {
        val errors = mutableListOf<String>()

        // In a real system, you would check for:
        // - Active competitions/entries
        // - Historical records that should be preserved
        // - Breeding records
        // - License dependencies

        // For now, we'll implement basic checks

        // Example: Check if horse has OEPS or FEI registration
        val horse = horseRepository.findById(request.pferdId)
        if (horse != null) {
            if (horse.isOepsRegistered() && !request.forceDelete) {
                errors.add("Cannot delete OEPS registered horse without force delete flag")
            }

            if (horse.isFeiRegistered() && !request.forceDelete) {
                errors.add("Cannot delete FEI registered horse without force delete flag")
            }

            // Check if horse has breeding information (might be important for pedigree)
            if ((horse.vaterName != null || horse.mutterName != null) && !request.forceDelete) {
                errors.add("Horse has pedigree information that might be referenced by other horses")
            }
        }

        return errors
    }

    /**
     * Generates warnings about the deletion.
     */
    private fun generateWarnings(horseName: String, oepsNummer: String?, feiNummer: String?): List<String> {
        val warnings = mutableListOf<String>()

        warnings.add("Horse '$horseName' will be permanently deleted")

        if (!oepsNummer.isNullOrBlank()) {
            warnings.add("OEPS registration number '$oepsNummer' will be lost")
        }

        if (!feiNummer.isNullOrBlank()) {
            warnings.add("FEI registration number '$feiNummer' will be lost")
        }

        warnings.add("This action cannot be undone")

        return warnings
    }

    /**
     * Batch delete multiple horses.
     */
    suspend fun batchDelete(horseIds: List<Uuid>, forceDelete: Boolean = false): BatchDeleteResponse {
        val results = mutableListOf<DeleteResult>()
        var successCount = 0
        var errorCount = 0

        for (horseId in horseIds) {
            val request = DeleteHorseRequest(horseId, forceDelete)
            val response = execute(request)

            results.add(
                DeleteResult(
                    horseId = horseId,
                    success = response.success,
                    errors = response.errors,
                    warnings = response.warnings
                )
            )

            if (response.success) {
                successCount++
            } else {
                errorCount++
            }
        }

        return BatchDeleteResponse(
            results = results,
            successCount = successCount,
            errorCount = errorCount,
            totalCount = horseIds.size
        )
    }

    /**
     * Result for individual horse deletion in batch operation.
     */
    data class DeleteResult(
        val horseId: Uuid,
        val success: Boolean,
        val errors: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    )

    /**
     * Response for batch delete operation.
     */
    data class BatchDeleteResponse(
        val results: List<DeleteResult>,
        val successCount: Int,
        val errorCount: Int,
        val totalCount: Int
    ) {
        val overallSuccess: Boolean = errorCount == 0
    }
}
