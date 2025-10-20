import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    // Applies the Kotlin JVM plugin to the project.
    kotlin("jvm")
    // Applies the Detekt plugin for static code analysis.
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    // Configures the Kotlin JVM toolchain to use JDK 17.
    jvmToolchain(17)
    // Kotlin compiler options for strict nullability interop and consistent behavior across modules.
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

// Ensure Java toolchain is also pinned to 17 for any Java sources.
extensions.configure(JavaPluginExtension::class.java) {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    // Adds the Maven Central repository to the list of repositories.
    mavenCentral()
}

val catalogs = project.extensions.getByType<VersionCatalogsExtension>()
val libs = catalogs.named("libs")

dependencies {
    // Import Spring Boot BOM via platform for implementation and test scopes.
    val springBootVersion = libs.findVersion("springBoot").get().requiredVersion
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))

    // Common test dependencies for all modules.
    testImplementation(libs.findLibrary("kotlin-test").get())
    testImplementation(libs.findLibrary("mockito-kotlin").get())
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

tasks {
    test {
        // Configures the test task to use the JUnit Platform.
        useJUnitPlatform()
        // Improve test diagnostics and parallelism by default.
        testLogging {
            events("failed", "skipped")
            exceptionFormat = TestExceptionFormat.FULL
            showStackTraces = true
            showStandardStreams = false
        }
        maxParallelForks = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
    }

    // Make static analysis part of the standard verification lifecycle.
    named("check") {
        dependsOn("detekt")
    }
}

// Centralized Detekt configuration so all modules inherit consistent rules and reports.
detekt {
    buildUponDefaultConfig = true
    parallel = true
    autoCorrect = false
}

// Configure Detekt reports on the tasks (extension-level reports are deprecated).
tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}
