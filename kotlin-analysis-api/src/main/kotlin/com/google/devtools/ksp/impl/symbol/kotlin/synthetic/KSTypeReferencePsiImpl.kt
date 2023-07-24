package com.google.devtools.ksp.impl.symbol.kotlin.synthetic

import com.google.devtools.ksp.ExceptionMessage
import com.google.devtools.ksp.KSObjectCache
import com.google.devtools.ksp.impl.symbol.kotlin.KSClassDeclarationImpl
import com.google.devtools.ksp.impl.symbol.kotlin.KSErrorType
import com.google.devtools.ksp.impl.symbol.kotlin.analyze
import com.google.devtools.ksp.symbol.*
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals

class KSTypeReferencePsiImpl private constructor(val psiElement: PsiElement, override val parent: KSNode) :
    KSTypeReference {

    companion object : KSObjectCache<KSNode, KSTypeReferencePsiImpl>() {
        fun getCached(psiElement: PsiElement, parent: KSNode) = cache
            .getOrPut(parent) { KSTypeReferencePsiImpl(psiElement, parent) }
    }

    @OptIn(KtAnalysisApiInternals::class)
    val type: KSType by lazy {
        when (psiElement) {
            is PsiAnnotation -> {
                val psiClass = psiElement.nameReferenceElement!!.resolve() as? PsiClass
                psiClass?.let {
                    analyze { analysisSession.symbolProviderByJavaPsi.getNamedClassSymbol(it)?.let {
                        KSClassDeclarationImpl.getCached(it)
                    } }
                }?.asStarProjectedType() ?: KSErrorType
            }
            else -> throw IllegalStateException(
                "Unexpected psi type in KSTypeReferencePsiImpl: ${psiElement.javaClass}, $ExceptionMessage"
            )
        }
    }

    override val origin = Origin.JAVA

    override val location: Location = NonExistLocation

    override val element: KSReferenceElement by lazy {
        TODO()
    }

    override val annotations: Sequence<KSAnnotation> = emptySequence()

    override val modifiers: Set<Modifier> = emptySet()

    override fun resolve(): KSType {
        return type
    }

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        return visitor.visitTypeReference(this, data)
    }

    override fun toString(): String {
        return type.toString()
    }
}
