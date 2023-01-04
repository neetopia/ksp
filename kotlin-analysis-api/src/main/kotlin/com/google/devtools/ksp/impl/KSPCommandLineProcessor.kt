/*
 * Copyright 2022 Google LLC
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.ksp.impl

import com.google.devtools.ksp.KspOptions
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.config.addJavaSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.util.ServiceLoaderLite
import java.io.File
import java.net.URLClassLoader

class KSPCommandLineProcessor(val compilerConfiguration: CompilerConfiguration) {
    private val kspOptionsBuilder = KspOptions.Builder()

    val kspOptions: KspOptions
        get() = kspOptionsBuilder.build()

    lateinit var providers: List<SymbolProcessorProvider>

    fun processArgs(args: Array<String>) {
        // TODO: support KSP options
        val sources = args.toList()
        compilerConfiguration.put(CommonConfigurationKeys.MODULE_NAME, "main")
        compilerConfiguration.addKotlinSourceRoots(sources)
        compilerConfiguration.addJavaSourceRoots(sources.map { File(it) })
        compilerConfiguration.addKotlinSourceRoots(listOf(kspOptions.kspOutputDir.absolutePath))
        compilerConfiguration.addJavaSourceRoots(listOf(kspOptions.kspOutputDir))
        compilerConfiguration.addJvmClasspathRoot(File("/Users/jiaxiang/.m2/repository/com/google/devtools/ksp/symbol-processing-analysis-api/2.0.255-SNAPSHOT/test-processor-1.0-SNAPSHOT.jar"))
        compilerConfiguration.addJvmClasspathRoot(File("/Users/jiaxiang/.m2/repository/com/google/devtools/ksp/symbol-processing-analysis-api/2.0.255-SNAPSHOT/kotlin-stdlib.jar"))
        compilerConfiguration.addJvmClasspathRoot(File("/Users/jiaxiang/.m2/repository/com/google/devtools/ksp/symbol-processing-analysis-api/2.0.255-SNAPSHOT/kotlin-script-runtime.jar"))
        compilerConfiguration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val processingClasspath = kspOptionsBuilder.processingClasspath + File("/Users/jiaxiang/.m2/repository/com/google/devtools/ksp/symbol-processing-analysis-api/2.0.255-SNAPSHOT/test-processor-1.0-SNAPSHOT.jar")
        val classLoader = URLClassLoader(
            processingClasspath.map { it.toURI().toURL() }.toTypedArray(),
            javaClass.classLoader
        )

        providers = ServiceLoaderLite.loadImplementations(SymbolProcessorProvider::class.java, classLoader)
    }
}
