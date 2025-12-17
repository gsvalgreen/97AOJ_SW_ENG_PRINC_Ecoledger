plugins {
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    java
    id("jacoco")
    id("org.sonarqube") version "7.2.0.6526"
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

sonar {
    properties {
        property("sonar.projectName", "ECO Ledger - Certificacao Service")
        property("sonar.java.coveragePlugin", "jacoco")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport.xml"
        )
    }
}

sourceSets {
    val integrationTest by creating {
        java.srcDir("src/integration-test/java")
        resources.srcDir("src/integration-test/resources")
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.2")

    integrationTestImplementation("com.h2database:h2")
    integrationTestImplementation("org.springframework.kafka:spring-kafka-test")
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

tasks.jacocoTestReport {
    dependsOn(tasks.test, integrationTest)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val sourceSetsMain = sourceSets["main"]
    classDirectories.setFrom(files(sourceSetsMain.output))
    executionData.setFrom(
        files(
            file("$buildDir/jacoco/test.exec"),
            file("$buildDir/jacoco/integrationTest.exec")
        )
    )
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
    executionData.setFrom(
        files(
            file("$buildDir/jacoco/test.exec"),
            file("$buildDir/jacoco/integrationTest.exec")
        )
    )
}

tasks.check {
    dependsOn(integrationTest)
    dependsOn("jacocoTestReport")
}
