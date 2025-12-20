plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

dependencies {
    implementation(platform(projects.platform.platformBom))
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
