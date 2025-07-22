plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(projects.platform.platformDependencies)

    implementation(projects.members.membersDomain)
    implementation(projects.members.membersApplication)

    implementation("org.springframework:spring-web")
    implementation("org.springdoc:springdoc-openapi-starter-common")

    testImplementation(projects.platform.platformTesting)
}
