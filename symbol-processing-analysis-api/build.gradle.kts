import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

evaluationDependsOn(":common-util")
evaluationDependsOn(":kotlin-analysis-api")

val kotlinBaseVersion: String by project
val signingKey: String? by project
val signingPassword: String? by project

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "6.0.0"
    `maven-publish`
    signing
}

val packedJars by configurations.creating

dependencies {
    packedJars("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:0.3.4")
    packedJars(kotlin("stdlib", kotlinBaseVersion))
    packedJars("org.jetbrains.kotlin:kotlin-compiler:$kotlinBaseVersion")

    packedJars("org.jetbrains.kotlin:high-level-api-fir-for-ide:$kotlinBaseVersion") {
        isTransitive = false
    }
    packedJars("org.jetbrains.kotlin:high-level-api-for-ide:$kotlinBaseVersion") {
        isTransitive = false
    }
    packedJars("org.jetbrains.kotlin:low-level-api-fir-for-ide:$kotlinBaseVersion") {
        isTransitive = false
    }
    packedJars("org.jetbrains.kotlin:analysis-api-providers-for-ide:$kotlinBaseVersion") {
        isTransitive = false
    }
    packedJars("org.jetbrains.kotlin:analysis-project-structure-for-ide:$kotlinBaseVersion") {
        isTransitive = false
    }
    packedJars("org.jetbrains.kotlin:symbol-light-classes-for-ide:$kotlinBaseVersion") {
        isTransitive = false
    }
    packedJars("org.jetbrains.kotlin:analysis-api-standalone-for-ide:$kotlinBaseVersion") {
        isTransitive = false
    }
    packedJars("org.jetbrains.kotlin:high-level-api-impl-base-for-ide:$kotlinBaseVersion") {
        isTransitive = false
    }
    packedJars(project(":kotlin-analysis-api")) { isTransitive = false }
    packedJars(project(":common-util")) { isTransitive = true }
}

tasks.withType<ShadowJar>() {
    archiveClassifier.set("")
    // ShadowJar picks up the `compile` configuration by default and pulls stdlib in.
    // Therefore, specifying another configuration instead.
    configurations = listOf(packedJars)
}

tasks {
    publish {
        dependsOn(shadowJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            artifactId = "symbol-processing-analysis-api"
            artifact(tasks["shadowJar"])
            pom {
                name.set("com.google.devtools.ksp:symbol-processing-analysis-api")
                description.set("Symbol processing for analysis api")
                withXml {
                    fun groovy.util.Node.addDependency(
                        groupId: String,
                        artifactId: String,
                        version: String,
                        scope: String = "runtime"
                    ) {
                        appendNode("dependency").apply {
                            appendNode("groupId", groupId)
                            appendNode("artifactId", artifactId)
                            appendNode("version", version)
                            appendNode("scope", scope)
                        }
                    }

                    asNode().appendNode("dependencies").apply {
                        addDependency("org.jetbrains.kotlin", "kotlin-stdlib", kotlinBaseVersion)
                        addDependency("org.jetbrains.kotlin", "kotlin-compiler", kotlinBaseVersion)
                        addDependency("com.google.devtools.ksp", "symbol-processing-api", version)
                    }
                }
            }
        }
    }
}

repositories {
    flatDir {
        dirs("${project.rootDir}/third_party/prebuilt/repo/")
    }
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap/")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-ide-plugin-dependencies")
    maven("https://www.jetbrains.com/intellij-repository/releases")
}
