buildscript {
    // Define versions in a single place
    extra.apply {
        set("pomFile", file("${project.buildDir}/generated-pom.xml"))
    }
}

plugins {
    idea
    kotlin("multiplatform") version "1.3.30"
  //  id("kotlinx-serialization") version "1.3.21"
    id("org.jetbrains.dokka") version "0.9.18"
    maven
    signing
    id("io.codearte.nexus-staging") version "0.11.0"
}

project.version = "0.1.0"
project.group = "com.github.wakingrufus"

repositories {
    mavenCentral()
    jcenter()
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "5.2"
    distributionType = Wrapper.DistributionType.ALL
}

kotlin {
    jvm {
        mavenPublication {
            artifactId = "filedb-jvm"
            // Add a docs JAR artifact (it should be a custom task):
            artifact(dokkaJar)
        }
    }
}
//    linuxX64("linux") {
//        binaries {
//            sharedLib()
//        }
//    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(kotlin("reflect"))
                implementation("io.github.microutils:kotlin-logging:1.6.23")
                implementation("org.slf4j:slf4j-api:1.7.25")

                implementation("com.fasterxml.jackson.core:jackson-core:2.9.8   ")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
                implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.9.8")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
                implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")

                // implementation(enforcedPlatform("com.fasterxml.jackson:jackson-bom:2.9.8"))

            }
        }
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation("org.slf4j:slf4j-log4j12:1.7.25")
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.assertj:assertj-core:3.12.2")
            }
        }
//        val linuxMain by getting {
//            dependencies {
//            }
//        }
//        val linuxTest by getting {
//            dependencies {
//            }
//        }
    }
}

task<Jar>("sourcesJar"){
    classifier = "sources"
    from("src/commonMain/kotlin", "src/jvmMain/kotlin")
}

val dokka by tasks.creating(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
    impliedPlatforms = ["Common"] // This will force platform tags for all non-common sources e.g. "JVM"
    kotlinTasks {
        // dokka fails to retrieve sources from MPP-tasks so they must be set empty to avoid exception
        // use sourceRoot instead (see below)
        []
    }
    sourceRoot {
        // assuming there is only a single source dir...
        path = kotlin.sourceSets.commonMain.kotlin.srcDirs[0]
        platforms = ["Common"]
    }
    sourceRoot {
        // assuming there is only a single source dir...
        path = kotlin.sourceSets.jvmMain.kotlin.srcDirs[0]
        platforms = ["JVM"]
    }
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(dokka)
}




apply { from("publish.gradle") }