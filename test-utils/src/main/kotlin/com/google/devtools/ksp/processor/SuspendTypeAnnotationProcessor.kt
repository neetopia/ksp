package com.google.devtools.ksp.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated

class SuspendTypeAnnotationProcessor : AbstractTestProcessor() {
    val result = mutableListOf<String>()

    override fun toResult(): List<String> {
        return result
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getClassDeclarationByName("MainBinding")!!.getConstructors().forEach {
            it.parameters.forEach { result.add(it.type.resolve().toString()) }
        }
        return emptyList()
    }
}
