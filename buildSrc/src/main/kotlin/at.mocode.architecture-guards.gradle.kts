import at.mocode.gradle.ForbiddenAuthorizationHeaderTask
import at.mocode.gradle.FeatureIsolationTask

tasks.register<ForbiddenAuthorizationHeaderTask>("archGuardForbiddenAuthorizationHeader") {
    group = "verification"
    description = "Fail build if code sets Authorization header manually."
}

tasks.register<FeatureIsolationTask>("archGuardNoFeatureToFeatureDeps") {
    group = "verification"
    description = "Fail build if a :frontend:features:* module depends on another :frontend:features:* module"
}

tasks.register("archGuards") {
    group = "verification"
    description = "Run all architecture guard checks"
    dependsOn("archGuardForbiddenAuthorizationHeader")
    dependsOn("archGuardNoFeatureToFeatureDeps")
}
