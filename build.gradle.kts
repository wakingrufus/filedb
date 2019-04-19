plugins {
    idea
    kotlin("multiplatform") version "1.3.21"
    id("kotlinx-serialization") version "1.3.21"
}

version = "0.1.0"
group = "com.github.wakingrufus"

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
        val main by compilations.getting {
            kotlinOptions {
                // Setup the Kotlin compiler options for the 'main' compilation:
                jvmTarget = "1.8"
            }
        }
        val test by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
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
        val jvmMain by getting {
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
        val jvmTest by getting {
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
