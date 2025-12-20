plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

dependencies {
    implementation(platform(projects.platform.platformBom))
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.bundles.spring.cloud.gateway) // Teils für Filter nützlich
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
