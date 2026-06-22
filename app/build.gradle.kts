plugins {
    // Applies the common conventions plugin for the URL shortener project.
    id("urlshortener-common-conventions")
    // Applies the Kotlin Spring plugin using an alias from the version catalog.
    alias(libs.plugins.kotlin.spring)
    // Applies the Spring Boot plugin using an alias from the version catalog.
    alias(libs.plugins.spring.boot)
}

dependencies {
    // Adds the core project as an implementation dependency.
    implementation(project(":core"))
    // Adds the delivery project as an implementation dependency.
    implementation(project(":delivery"))
    // Adds the repositories project as an implementation dependency.
    implementation(project(":repositories"))

    // Adds the Spring Boot starter as an implementation dependency.
    implementation(libs.spring.boot.starter)
    // Adds Spring Boot Actuator for monitoring and management endpoints.
    implementation(libs.spring.boot.starter.actuator)
    // Adds Bootstrap as an implementation dependency.
    implementation(libs.bootstrap)
    // Adds OpenAPI/Swagger documentation as an implementation dependency.
    implementation(libs.springdoc.openapi)

    // Adds HSQLDB as a runtime-only dependency.
    runtimeOnly(libs.hsqldb)
    // Adds Kotlin reflection library as a runtime-only dependency.
    runtimeOnly(libs.kotlin.reflect)

    // Adds Spring Boot starter test library as a test implementation dependency.
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.restclient)
    testImplementation(libs.spring.boot.resttestclient)
    testImplementation(libs.jackson.module.kotlin)
    // Adds Spring Boot starter JDBC library as a test implementation dependency.
    testImplementation(libs.spring.boot.starter.jdbc)
    // Adds Apache HttpClient 5 as a test implementation dependency.
    testImplementation(libs.httpclient5)
}

