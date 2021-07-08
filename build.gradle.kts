@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val s2client_version: String by project
val kodein_version: String by project
val junit_version: String by project
val strikt_version: String by project

group = "de.stefanbissell.numbsi"
version = "0.1"

plugins {
    application
    kotlin("jvm")
    id("com.github.ben-manes.versions")
    id("com.github.johnrengelman.shadow")
    id("com.adarshr.test-logger")
}

application {
    mainClass.set("de.stefanbissell.bots.numbsi.MainKt")
}

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

    jar {
        enabled = false
    }

    shadowJar {
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to application.mainClass.get()
                )
            )
        }
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        archiveVersion.set("")
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
