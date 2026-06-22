plugins {
    // Apply the common conventions plugin for the URL shortener project
    id("urlshortener-common-conventions")

    // Apply the Kotlin JPA plugin
    alias(libs.plugins.kotlin.jpa)

    // Apply the Spring Boot plugin without automatically applying it
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    // Add the core project as an implementation dependency
    implementation(project(":core"))

    // Add the Spring Boot Starter Data JPA library as an implementation dependency
    implementation(libs.spring.boot.starter.data.jpa)
}

