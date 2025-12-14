plugins {
    java
    idea
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val cucumberVersion = "7.33.0"
val junitBomVersion = "5.14.1"
val cucumberReportsPath = "build/reports/cucumber"

dependencies {

    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("org.junit.platform:junit-platform-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.slf4j:slf4j-simple:2.0.9")
    testImplementation("org.postgresql:postgresql:42.7.7")
}

tasks.test {
    useJUnitPlatform()
    val cucumberPlugins = listOf(
        "pretty",
        "html:$cucumberReportsPath/index.html",
        "json:$cucumberReportsPath/report.json",
        "summary"
    )
    systemProperty("cucumber.plugin", cucumberPlugins.joinToString(","))
    systemProperty("cucumber.publish.quiet", "true")
    doFirst {
        file(cucumberReportsPath).mkdirs()
    }
    outputs.dir(cucumberReportsPath)
}
