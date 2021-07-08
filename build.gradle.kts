@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlin_version: String by project
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
    implementation("com.github.ocraft:ocraft-s2client-bot:0.4.7")
    implementation("org.kodein.di:kodein-di:7.6.0")

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
