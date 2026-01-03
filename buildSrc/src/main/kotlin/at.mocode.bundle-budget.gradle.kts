import at.mocode.gradle.BundleBudgetTask

tasks.register<BundleBudgetTask>("checkBundleBudget") {
    group = "verification"
    description = "Checks JS bundle sizes of frontend shells against configured budgets"
}
