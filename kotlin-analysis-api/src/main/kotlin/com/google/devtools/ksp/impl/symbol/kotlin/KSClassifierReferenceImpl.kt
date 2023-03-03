package com.google.devtools.ksp.impl.symbol.kotlin

import com.google.devtools.ksp.IdKeyTriple
import com.google.devtools.ksp.KSObjectCache
import com.google.devtools.ksp.symbol.*
import org.jetbrains.kotlin.analysis.api.types.KtClassType
import org.jetbrains.kotlin.analysis.api.types.KtClassTypeQualifier
import org.jetbrains.kotlin.analysis.api.types.KtTypeParameterType

class KSClassifierReferenceImpl private constructor(
    internal val ktType: KtClassType,
    internal val index: Int,
    override val parent: KSTypeReference?
) : KSClassifierReference {
    companion object : KSObjectCache<IdKeyTriple<KtClassType, Int, KSTypeReference?>, KSClassifierReferenceImpl>() {
        fun getCached(ktType: KtClassType, parent: KSTypeReference?, index: Int = 0) =
            cache.getOrPut(IdKeyTriple(ktType, index, parent)) { KSClassifierReferenceImpl(ktType, index, parent) }
    }

    private val classifierReference: KtClassTypeQualifier
        get() = ktType.qualifiers[index]

    override val qualifier: KSClassifierReference? by lazy {
        if (index == ktType.qualifiers.size - 1) {
            null
        } else {
            getCached(ktType, parent, index+1)
        }
    }

    override fun referencedName(): String {
        return classifierReference.name.asString()
    }

    override val typeArguments: List<KSTypeArgument> by lazy {
        classifierReference.typeArguments.map { KSTypeArgumentImpl.getCached(it, this) }
    }

    override val origin: Origin = parent?.origin ?: Origin.SYNTHETIC

    override val location: Location
        get() = parent?.location ?: NonExistLocation
}

class KSClassifierParameterImpl private constructor(
    internal val ktType: KtTypeParameterType,
    override val parent: KSTypeReference?
) : KSClassifierReference {
    override val qualifier: KSClassifierReference?
        get() = TODO("Not yet implemented")

    override fun referencedName(): String {
        TODO("Not yet implemented")
    }

    override val typeArguments: List<KSTypeArgument>
        get() = emptyList()
    override val origin: Origin
        get() = TODO("Not yet implemented")
    override val location: Location
        get() = TODO("Not yet implemented")

}
