plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Set the name of the root project
rootProject.name = "urlshortener"

// Include the specified subprojects in the build
include("core", "delivery", "repositories", "app")
