package at.mocode.archtests

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

// Scan ALL project classes from the root package
@AnalyzeClasses(packages = ["at.mocode"])
class BackendArchitectureTest {

    @ArchTest
    fun `service modules should not depend on each other`(importedClasses: JavaClasses) {
        val servicePackages = listOf(
            "at.mocode.ping..",
            "at.mocode.entries.."
            // Add other service packages here as they are created
        )

        for (servicePackage in servicePackages) {
            val otherServicePackages = servicePackages.filter { it != servicePackage }.toTypedArray()
            if (otherServicePackages.isEmpty()) continue

            noClasses()
                .that().resideInAPackage(servicePackage)
                .should().accessClassesThat().resideInAnyPackage(*otherServicePackages)
                .check(importedClasses)
        }
    }
}
