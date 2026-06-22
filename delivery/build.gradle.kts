plugins {
    // Apply the common conventions plugin for the project
    id("urlshortener-common-conventions")

    // Apply the Kotlin JPA plugin
    alias(libs.plugins.kotlin.jpa)

    // Apply the Kotlin Spring plugin
    alias(libs.plugins.kotlin.spring)

    // Apply the Spring Boot plugin but do not apply it immediately
    alias(libs.plugins.spring.boot) apply false
}

dependencies {
    // Include the core project as an implementation dependency
    implementation(project(":core"))

    // Add Kotlin Logging for idiomatic Kotlin logging
    implementation(libs.kotlin.logging)

    // Include Spring Boot Starter Web as an implementation dependency
    implementation(libs.spring.boot.starter.webmvc)

    // Include Spring Boot Starter HATEOAS as an implementation dependency
    implementation(libs.spring.boot.starter.hateoas)

    // Include Apache Commons Validator as an implementation dependency
    implementation(libs.commons.validator)

    // Include Google Guava as an implementation dependency
    implementation(libs.guava)

    // Include OpenAPI/Swagger documentation as an implementation dependency
    implementation(libs.springdoc.openapi)

    // Include Spring Boot Starter Test as a test implementation dependency
    testImplementation(libs.spring.boot.starter.webmvc.test)
}
