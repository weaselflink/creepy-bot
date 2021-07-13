@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val s2client_version: String by project
val kodein_version: String by project
val junit_version: String by project
val strikt_version: String by project
val mockk_version: String by project

group = "de.stefanbissell.bots.numbsi"
version = "0.1"

plugins {
    application
    kotlin("jvm")
    id("com.github.ben-manes.versions")
    id("com.github.johnrengelman.shadow")
    id("com.adarshr.test-logger")
}

application {
    mainClass.set("de.stefanbissell.bots.numbsi.LadderKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ocraft:ocraft-s2client-bot:$s2client_version")
    implementation("org.kodein.di:kodein-di:$kodein_version")

    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
    testImplementation("io.strikt:strikt-core:$strikt_version")
    testImplementation("io.mockk:mockk:$mockk_version")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    val botZip = register<Zip>("botZip") {
        archiveFileName.set("${rootProject.name}.zip")
        destinationDirectory.set(layout.buildDirectory.dir("dist"))

        from(layout.buildDirectory.dir("libs"))
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

        finalizedBy(botZip)
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    withType<Tar> {
        enabled = false
    }

    distZip {
        enabled = false
    }

    shadowDistZip {
        enabled = false
    }
}
