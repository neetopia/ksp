package com.google.devtools.ksp.visitor

import com.google.devtools.ksp.symbol.*

class KSValidateVisitor(private val shouldValidate: (KSNode, KSNode) -> Boolean = { _, _-> true } ) : KSDefaultVisitor<Unit, Boolean>() {
    private fun visitDeclarations(declarationContainer: KSDeclarationContainer): Boolean {
        return declarationContainer.declarations.any { shouldValidate(declarationContainer, it) && !it.accept(this, Unit) }
    }

    override fun defaultHandler(node: KSNode, data: Unit): Boolean {
        return false
    }

    override fun visitTypeReference(typeReference: KSTypeReference, data: Unit): Boolean {
        return !typeReference.resolve().isError
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Boolean {
        if (classDeclaration.typeParameters.any { shouldValidate(classDeclaration, it) && !it.accept(this, Unit) }) {
            return false
        }
        if (classDeclaration.asStarProjectedType().isError) {
            return false
        }
        if (!visitDeclarations(classDeclaration)) {
            return false
        }
        return true
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit): Boolean {
        if (function.typeParameters.any { shouldValidate(function, it) && !it.accept(this, Unit) }) {
            return false
        }
        if (function.returnType?.accept(this, data) != true) {
            return false
        }
        if (!visitDeclarations(function)) {
            return false
        }
        return true
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit): Boolean {
        if (property.typeParameters.any { shouldValidate(property, it) && !it.accept(this, Unit) }) {
            return false
        }
        if (property.type.resolve().isError) {
            return false
        }
        return true
    }

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit): Boolean {
        if (typeParameter.bounds.any { shouldValidate(typeParameter, it) && !it.accept(this, Unit) }) {
            return false
        }
        return true
    }
}