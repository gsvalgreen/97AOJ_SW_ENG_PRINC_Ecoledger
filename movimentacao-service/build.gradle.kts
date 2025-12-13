plugins {
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    java
    id("jacoco")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = "0.8.13"
}

sourceSets {
    val integrationTest by creating {
        java.srcDir("src/integration-test/java")
        resources.srcDir("src/integration-test/resources")
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += output + compileClasspath
    }
    val featureTest by creating {
        java.srcDir("src/feature-test/java")
        resources.srcDir("src/feature-test/resources")
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += output + compileClasspath
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}
val featureTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val featureTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("software.amazon.awssdk:s3:2.40.6")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-junit-jupiter")
    integrationTestImplementation("com.h2database:h2")
    integrationTestImplementation("org.springframework.kafka:spring-kafka-test")
    integrationTestImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.2")
    integrationTestImplementation("com.adobe.testing:s3mock:4.11.0")
    integrationTestImplementation("com.adobe.testing:s3mock-testcontainers:4.11.0")
    integrationTestImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

val featureTest = tasks.register<Test>("featureTest") {
    description = "Runs feature tests against local instance"
    group = "verification"
    testClassesDirs = sourceSets["featureTest"].output.classesDirs
    classpath = sourceSets["featureTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, integrationTest)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val sourceSetsMain = sourceSets["main"]
    classDirectories.setFrom(files(sourceSetsMain.output))
    executionData.setFrom(fileTree(buildDir).include("**/jacoco/*.exec", "**/*.exec"))
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.8".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(files(sourceSets["main"].output))
    executionData.setFrom(fileTree(buildDir).include("**/jacoco/*.exec", "**/*.exec"))
}

tasks.check {
    dependsOn(integrationTest)
    dependsOn("jacocoTestReport")
}
