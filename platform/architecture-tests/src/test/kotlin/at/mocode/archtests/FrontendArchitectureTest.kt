package at.mocode.archtests

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

// Scan ALL project classes from the root package
@AnalyzeClasses(packages = ["at.mocode"])
class FrontendArchitectureTest {

  @ArchTest
  fun `feature modules should not depend on each other`(importedClasses: JavaClasses) {
    // The pattern must match the actual package structure, e.g., 'at.mocode.ping.feature'
    slices()
      .matching("at.mocode.(*).feature..")
      .should().notDependOnEachOther()
      .check(importedClasses)
  }
}
