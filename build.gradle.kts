buildscript {
    // Define versions in a single place
    extra.apply {
        set("pomFile", file("${project.buildDir}/generated-pom.xml"))
    }
}

plugins {
    idea
    kotlin("jvm") version "1.3.30"
    id("org.jetbrains.dokka") version "0.9.18"
    maven
    signing
    jacoco
    id("io.codearte.nexus-staging") version "0.11.0"
}

project.version = "0.2.0"
project.group = "com.github.wakingrufus"

repositories {
    mavenCentral()
    jcenter()
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "5.2"
    distributionType = Wrapper.DistributionType.ALL
}


dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.github.microutils:kotlin-logging:1.6.23")
    implementation("org.slf4j:slf4j-api:1.7.25")

    implementation("com.fasterxml.jackson.core:jackson-core:2.9.8   ")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.9.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")

    testImplementation("org.slf4j:slf4j-log4j12:1.7.25")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("org.nield:kotlin-statistics:1.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")

}

task<Jar>("sourcesJar") {
    classifier = "sources"
    from("src/main/kotlin")
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(tasks.dokka)
}

apply { from("publish.gradle") }