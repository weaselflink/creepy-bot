@file:Suppress("LocalVariableName")

rootProject.name = "numbsi-bot"

pluginManagement {
    val kotlin_version: String by settings
    val versions_version: String by settings
    val test_logger_version: String by settings
    plugins {
        kotlin("jvm") version kotlin_version
        id("com.github.ben-manes.versions") version versions_version
        id("com.adarshr.test-logger") version test_logger_version
    }
}
