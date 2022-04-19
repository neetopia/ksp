pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://www.jetbrains.com/intellij-repository/snapshots")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
    val kotlinBaseVersion: String = java.util.Properties().also { props ->
        settingsDir.parentFile.resolve("gradle.properties").inputStream().use {
            props.load(it)
        }
    }["kotlinBaseVersion"] as String
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinBaseVersion
    }
}
