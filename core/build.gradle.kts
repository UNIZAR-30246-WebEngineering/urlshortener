plugins {
    // Apply the common conventions plugin for the URL shortener project
    id("urlshortener-common-conventions")
}

dependencies {
    // Add Kotlin Logging for idiomatic Kotlin logging
    implementation(libs.kotlin.logging)

    // Add SLF4J Simple for logging implementation (version managed by Spring Boot BOM)
    testImplementation(libs.slf4j.simple)

    // Add Kotlin test library for unit testing
    testImplementation(libs.kotlin.test)

    // Add Mockito Kotlin library for mocking in tests
    testImplementation(libs.mockito.kotlin)

    // Add JUnit Jupiter library for writing and running tests
    testImplementation(libs.junit.jupiter)

    // Add JUnit Platform Launcher for launching tests
    testRuntimeOnly(libs.junit.platform.launcher)
}
