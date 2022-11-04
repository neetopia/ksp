package com.google.devtools.ksp.impl.symbol.java

import com.google.devtools.ksp.KSObjectCache
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.impl.ResolverAAImpl
import com.google.devtools.ksp.impl.symbol.kotlin.KSNameImpl
import com.google.devtools.ksp.impl.symbol.kotlin.KSTypeImpl
import com.google.devtools.ksp.impl.symbol.kotlin.KSTypeReferenceImpl
import com.google.devtools.ksp.impl.symbol.kotlin.KSValueArgumentImpl
import com.google.devtools.ksp.impl.symbol.kotlin.analyze
import com.google.devtools.ksp.impl.symbol.kotlin.classifierSymbol
import com.google.devtools.ksp.impl.symbol.kotlin.getDefaultValue
import com.google.devtools.ksp.impl.symbol.kotlin.toLocation
import com.google.devtools.ksp.symbol.AnnotationUseSiteTarget
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Origin
import com.intellij.lang.jvm.JvmClassKind
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralValue
import com.intellij.psi.PsiReference
import org.jetbrains.kotlin.analysis.api.annotations.KtConstantAnnotationValue
import org.jetbrains.kotlin.analysis.api.annotations.KtNamedAnnotationValue
import org.jetbrains.kotlin.analysis.api.symbols.KtClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.types.KtType

class KSAnnotationJavaImpl private constructor(private val psi: PsiAnnotation) : KSAnnotation {
    companion object : KSObjectCache<PsiAnnotation, KSAnnotationJavaImpl>() {
        fun getCached(psi : PsiAnnotation) =
            KSAnnotationJavaImpl.cache.getOrPut(psi) { KSAnnotationJavaImpl(psi) }
    }

    private val type: KtType by lazy {
        (ResolverAAImpl.instance.getClassDeclarationByName(psi.qualifiedName!!)!!.asStarProjectedType() as KSTypeImpl).type
    }

    override val annotationType: KSTypeReference by lazy {
        KSTypeReferenceImpl.getCached(type, this)
    }

    override val arguments: List<KSValueArgument> by lazy {
        val annotationConstructor = analyze {
            (type.classifierSymbol() as KtClassOrObjectSymbol).getMemberScope().getConstructors().singleOrNull()
        }
        val presentArgs = psi.parameterList.attributes.mapIndexed { index, it ->
            val name = it.name ?: annotationConstructor?.valueParameters?.getOrNull(index)?.name?.asString()
            val value = it.value
            val calculatedValue: Any? = if (value is PsiArrayInitializerMemberValue) {
                value.initializers.map {
                    calcValue(it)
                }
            } else {
                calcValue(it.value)
            }
            KSValueArgumentLiteImpl.getCached(
                name?.let { KSNameImpl.getCached(it) },
                calculatedValue,
                Origin.JAVA
            )
        }
        val presentValueArgumentNames = presentArgs.map { it.name?.asString() ?: "" }
        presentArgs + defaultArguments.filter { it.name?.asString() !in presentValueArgumentNames }
    }

    override val defaultArguments: List<KSValueArgument> by lazy {
        analyze {
            (type.classifierSymbol() as KtClassOrObjectSymbol).getMemberScope().getConstructors().singleOrNull()?.let {
                it.valueParameters.mapNotNull { valueParameterSymbol ->
                    valueParameterSymbol.getDefaultValue()?.let { constantValue ->
                        KSValueArgumentImpl.getCached(
                            KtNamedAnnotationValue(
                                valueParameterSymbol.name, KtConstantAnnotationValue(constantValue),
                            ), Origin.SYNTHETIC
                        )
                    }
                }
            }
        } ?: emptyList()
    }

    override val shortName: KSName by lazy {
        KSNameImpl.getCached(psi.qualifiedName!!.split(".").last())
    }

    override val useSiteTarget: AnnotationUseSiteTarget? = null

    override val origin: Origin = Origin.JAVA

    override val location: Location
        get() = psi.toLocation()

    override val parent: KSNode?
        get() = TODO("Not yet implemented")

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        return visitor.visitAnnotation(this, data)
    }

    private fun calcValue(value: PsiAnnotationMemberValue?): Any? {
        if (value is PsiAnnotation) {
            return getCached(value)
        }
        val result = when (value) {
            is PsiReference -> value.resolve()?.let { resolved ->
                JavaPsiFacade.getInstance(value.project).constantEvaluationHelper.computeConstantExpression(value)
                    ?: resolved
            }
            else -> value?.let {
                JavaPsiFacade.getInstance(value.project).constantEvaluationHelper.computeConstantExpression(value)
            }
        }
        return when (result) {
            // is PsiType -> {
            //     resolveJavaTypeSimple(result)
            // }
            is PsiLiteralValue -> {
                result.value
            }
            is PsiField -> {
                // manually handle enums as constant expression evaluator does not seem to be resolving them.
                val containingClass = result.containingClass
                if (containingClass?.classKind == JvmClassKind.ENUM) {
                    // this is an enum entry
                    containingClass.qualifiedName?.let {
                        ResolverAAImpl.instance!!.getClassDeclarationByName(it)
                    }?.declarations?.find {
                        it is KSClassDeclaration && it.classKind == ClassKind.ENUM_ENTRY &&
                            it.simpleName.asString() == result.name
                    }?.let { (it as KSClassDeclaration).asStarProjectedType() }
                        ?.let {
                            return it
                        }
                } else {
                    null
                }
            }
            else -> result
        }
    }
}
