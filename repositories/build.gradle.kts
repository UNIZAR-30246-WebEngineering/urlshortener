plugins {
    // Apply the common conventions plugin for the URL shortener project
    id("urlshortener-common-conventions")

    // Apply the Kotlin JPA plugin
    alias(libs.plugins.kotlin.jpa)

    // Apply the Spring Boot plugin without automatically applying it
    alias(libs.plugins.spring.boot) apply false

    // Apply the Spring Dependency Management plugin
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Add the core project as an implementation dependency
    implementation(project(":core"))

    // Add the Spring Boot Starter Data JPA library as an implementation dependency
    implementation(libs.spring.boot.starter.data.jpa)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Add dependencies for testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockito.kotlin) // Mockito Kotlin extensions
    testImplementation(libs.junit.jupiter) // JUnit 5
    testRuntimeOnly(libs.junit.platform.launcher) // JUnit Platform Launcher
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("com.h2database:h2")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

dependencyManagement {
    imports {
        // Import the Spring Boot BOM (Bill of Materials) for dependency management
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        // Ensure that all dependencies from the org.jetbrains.kotlin group use version 1.9.23
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.9.23")
        }
    }
}
