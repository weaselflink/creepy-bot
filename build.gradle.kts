@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val s2client_version: String by project
val kodein_version: String by project
val junit_version: String by project
val strikt_version: String by project

plugins {
    kotlin("jvm")
    id("com.github.ben-manes.versions")
    id("com.adarshr.test-logger")
}

group = "de.stefanbissell.numbsi"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ocraft:ocraft-s2client-bot:$s2client_version")
    implementation("org.kodein.di:kodein-di:$kodein_version")

    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
    testImplementation("io.strikt:strikt-core:$strikt_version")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
