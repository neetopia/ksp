plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        withJava()
    }
    linuxX64()
    mingwX64()
    macosX64()
    ios()
    js(IR) {
        browser()
        nodejs()
    }
    sourceSets {
        val commonMain by getting
        val jvmMain by getting {
            dependencies {
                implementation(project(":test-processor"))
                project.dependencies.add("kspJvm", project(":test-processor"))
            }
            kotlin.srcDir("src/main/java")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xuse-deprecated-legacy-compiler"
}

ksp {
    arg("option1", "value1")
    arg("option2", "value2")
}

dependencies {
    add("kspCommonMainMetadata", project(":test-processor"))
    add("kspJvm", project(":test-processor"))
    add("kspJvmTest", project(":test-processor"))
    add("kspJs", project(":test-processor"))
    add("kspJsTest", project(":test-processor"))
    add("kspLinuxX64", project(":test-processor"))
    add("kspLinuxX64Test", project(":test-processor"))
    add("kspMingwX64", project(":test-processor"))
    add("kspMingwX64Test", project(":test-processor"))
}

afterEvaluate {
    configurations["jvmRuntimeClasspath"].files
}
