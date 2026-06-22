import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.toolchain.JavaLanguageVersion
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    // Applies the Kotlin JVM plugin to the project.
    kotlin("jvm")
    // Applies the Detekt plugin for static code analysis.
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    // Configures the Kotlin JVM toolchain to use JDK 25.
    jvmToolchain(25)
    // Kotlin compiler options for strict nullability interop and consistent behavior across modules.
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

// Ensure Java toolchain is also pinned to 25 for any Java sources.
extensions.configure(JavaPluginExtension::class.java) {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
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

    // detekt 1.23.x does not run on JDK 25; wire into check only on JDK ≤22.
    // CI runs detekt separately with JDK 21 (see ADR-007 / scaffold ci.yml).
    named("check") {
        if (JavaVersion.current() <= JavaVersion.VERSION_22) {
            dependsOn("detekt")
        }
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
    jvmTarget = "22"
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}
