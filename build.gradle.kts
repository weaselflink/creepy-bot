import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.github.ben-manes.versions")
}

group = "de.stefanbissell.creepy"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ocraft:ocraft-s2client-bot:0.4.7")
    implementation("org.kodein.di:kodein-di:7.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
