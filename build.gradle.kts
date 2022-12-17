import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    id("org.springframework.boot") version "2.7.3" apply false
    id("io.spring.dependency-management") version "1.0.13.RELEASE" apply false
    id("io.gitlab.arturbosch.detekt") version("1.22.0-RC1")
    id("org.sonarqube") version ("3.3")
    id("com.bmuschko.docker-remote-api") version "6.7.0"
    jacoco

    kotlin("jvm") version "1.7.10" apply false
    kotlin("plugin.spring") version "1.7.10" apply false
    kotlin("plugin.jpa") version "1.7.10" apply false
}

group = "es.unizar"
version = "0.2022.1-SNAPSHOT"

var mockitoVersion = "4.0.0"
var bootstrapVersion = "3.4.0"
var jqueryVersion = "3.6.1"
var guavaVersion = "31.1-jre"
var commonsValidatorVersion = "1.6"

tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
    reports {
        html.required.set(true)
        md.required.set(true)
    }
}
/*
val jacocoMerge by tasks.registering(JacocoMerge::class) {
    subprojects {
        executionData(tasks.withType<JacocoReport>().map {
            it.executionData
        })
    }
    destinationFile = file("$buildDir/jacoco")
}

tasks.register<JacocoReport>("jacocoRootReport") {
    description = "Generates an aggregate report from all subprojects"
    dependsOn(jacocoMerge)

    sourceDirectories.from(files(subprojects.map {
        it.the<SourceSetContainer>()["main"].allSource.srcDirs
    }))

    classDirectories.from(files(subprojects.map {
        it.the<SourceSetContainer>()["main"].output
    }))

    repositories {
        mavenLocal()
        mavenCentral()
    }

    executionData(jacocoMerge.get().destinationFile)
    reports { // <- adjust
        html.required
        xml.required
        csv.required
    }
}*/

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "jacoco")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.register<JacocoReport>("codeCoverageReport") {

        executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))
        println("EXECUTION DATA -> " + executionData.asPath)
        sourceSets(project.extensions.getByType(SourceSetContainer::class.java).getByName("main"))

        reports {
            html.required.set(true)
            xml.required.set(true)
            csv.required.set(true)
        }

        dependsOn(allprojects.map { it.tasks.named<Test>("test") })
    }

    jacoco {
        toolVersion = "0.8.8"
        println("BUILD DIR -> $buildDir")
        reportsDirectory.set(layout.buildDirectory.dir("$buildDir/reports/jacoco"))
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        "implementation"("org.springdoc:springdoc-openapi-data-rest:1.6.0")
        "implementation"("org.springdoc:springdoc-openapi-ui:1.6.0")
        "implementation"("org.springdoc:springdoc-openapi-kotlin:1.6.0")
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib")
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        "implementation"("com.squareup.retrofit2:retrofit:2.9.0")
        "implementation"("com.squareup.retrofit2:converter-gson:2.9.0")
    }

    detekt {
        source = objects.fileCollection().from(
            io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_SRC_DIR_JAVA,
            io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_TEST_SRC_DIR_JAVA,
            io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_SRC_DIR_KOTLIN,
            io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_TEST_SRC_DIR_KOTLIN,
        )
        buildUponDefaultConfig = true
        baseline = file("$rootDir/config/detekt/baseline.xml")
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "1.8"
        reports {
            html.required.set(true)
            md.required.set(true)
        }
    }

    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "1.8"
    }
}

// No dependencies should be added here
project(":core") {}

project(":repositories") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

    dependencies {
        "implementation"(project(":core"))
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
    }

    tasks.getByName<BootJar>("bootJar") {
        enabled = false
    }
}

project(":delivery") {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.sonarqube")

    dependencies {
        "implementation"(project(":core"))
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("org.springframework.boot:spring-boot-starter-hateoas")
        "implementation"("org.springframework.boot:spring-boot-starter-cache")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "implementation"("commons-validator:commons-validator:$commonsValidatorVersion")
        "implementation"("com.google.guava:guava:$guavaVersion")
        "implementation"("io.github.g0dkar:qrcode-kotlin-jvm:3.2.0")
        "implementation"("org.jfree:org.jfree.svg:5.0.3")
        "implementation"("com.bucket4j:bucket4j-core:8.1.1")
        "implementation"("org.springframework.amqp:spring-rabbit:3.0.0")
        "implementation"("org.springframework.boot:spring-boot-starter-rsocket")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.mockito.kotlin:mockito-kotlin:$mockitoVersion")

    }

    tasks.getByName<BootJar>("bootJar") {
        enabled = false
    }

    sonarqube {
        properties {
            val path = "$projectDir/build/jacoco/test.exec"
            if (File(path).exists()) {
                println("Configurando property $path")
                property("sonar.jacoco.reportPath", path)
            }
        }
    }
}

project(":app") {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.sonarqube")

    dependencies {
        "implementation"(project(":core"))
        "implementation"(project(":delivery"))
        "implementation"(project(":repositories"))
        "implementation"(project(":rabbitQueue"))
        "implementation"("org.springframework.boot:spring-boot-starter")
        "implementation"("org.webjars:bootstrap:$bootstrapVersion")
        "implementation"("org.webjars:jquery:$jqueryVersion")
        "implementation"("org.springframework.amqp:spring-rabbit:3.0.0")

        "runtimeOnly"("org.hsqldb:hsqldb")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.springframework.boot:spring-boot-starter-web")
        "testImplementation"("org.springframework.boot:spring-boot-starter-jdbc")
        "testImplementation"("org.mockito.kotlin:mockito-kotlin:$mockitoVersion")
        "testImplementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "testImplementation"("org.apache.httpcomponents:httpclient")
    }

    //docker services for starting/stopping rabbitmq
    tasks.register("rabbit", Exec::class) {
        description = "Starts the docker container for rabbitmq"
        commandLine = listOf("docker", "run", "-d", "--rm", "--name", "rabbitmq", "-p", "5672:5672", "-p", "15672:15672", "rabbitmq:3.11-management")
    }

    tasks.register("rabbit-stop", Exec::class) {
        description = "Stops the docker container for rabbitmq"
        commandLine = listOf("docker", "stop", "rabbitmq")
    }

    //docker services for starting sonarqube
    tasks.register("sonar", Exec::class) {
        description = "Starts the docker container for sonar"
        commandLine = listOf("docker-compose", "-f", "../sonarqube/docker-compose.yaml", "up", "-d")
    }

    //docker services for starting sonarqube
    tasks.register("sonar-stop", Exec::class) {
        description = "Starts the docker container for rabbitmq"
        commandLine = listOf("cd", "sonarqube")
        commandLine = listOf("docker-compose", "-f", "../sonarqube/docker-compose.yaml", "stop")
    }

    sonarqube {
        properties {
            val path = "$projectDir/build/jacoco/test.exec"
            if (File(path).exists()) {
                println("Configurando property $path")
                property("sonar.jacoco.reportPath", path)
            }
        }
    }
}

project(":rabbitQueue") {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    dependencies {
        "implementation"(project(":core"))
        "implementation"(project(":delivery"))
        "implementation"(project(":repositories"))
        "implementation"("org.springframework.boot:spring-boot-starter")
        "implementation"("org.webjars:bootstrap:$bootstrapVersion")
        "implementation"("org.webjars:jquery:$jqueryVersion")
        "implementation"("org.springframework.amqp:spring-rabbit:3.0.0")

        "runtimeOnly"("org.hsqldb:hsqldb")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.springframework.boot:spring-boot-starter-web")
        "testImplementation"("org.springframework.boot:spring-boot-starter-jdbc")
        "testImplementation"("org.mockito.kotlin:mockito-kotlin:$mockitoVersion")
        "testImplementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "testImplementation"("org.apache.httpcomponents:httpclient")
    }
}

project(":consoleApp") {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter-hateoas")
        "implementation"("org.springframework.boot:spring-boot-starter")
        "implementation"("org.springframework.boot:spring-boot-starter-rsocket")
    }
}


