package com.google.devtools.ksp.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated

class ParameterMutabilityProcessor : AbstractTestProcessor() {
    val result = mutableListOf<String>()

    override fun toResult(): List<String> {
        return result
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val libClass = resolver.getClassDeclarationByName("LibClass")!!
        val sourceClass = resolver.getClassDeclarationByName("SourceClass")!!
        libClass.primaryConstructor!!.parameters.forEach { result.add("${it.name!!.asString()}: isVal(): ${it.isVal} isVar(): ${it.isVar}") }
        sourceClass.primaryConstructor!!.parameters.forEach { result.add("${it.name!!.asString()}: isVal(): ${it.isVal} isVar(): ${it.isVar}") }
        return emptyList()
    }
}
