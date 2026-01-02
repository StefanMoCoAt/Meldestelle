plugins {
  id("java")
}

group = "at.mocode"
version = "1.0.0-SNAPSHOT"

// The 'repositories' block was removed from here.
// Repository configuration is now centralized in 'settings.gradle.kts'.

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
  useJUnitPlatform()
}
