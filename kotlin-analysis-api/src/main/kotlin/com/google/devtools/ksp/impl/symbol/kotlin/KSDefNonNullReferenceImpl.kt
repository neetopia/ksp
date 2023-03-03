package com.google.devtools.ksp.impl.symbol.kotlin

import com.google.devtools.ksp.IdKeyPair
import com.google.devtools.ksp.KSObjectCache
import com.google.devtools.ksp.symbol.*
import org.jetbrains.kotlin.analysis.api.types.KtClassType
import org.jetbrains.kotlin.analysis.api.types.KtDefinitelyNotNullType
import org.jetbrains.kotlin.analysis.api.types.KtUsualClassType

class KSDefNonNullReferenceImpl private constructor(
    val ktDefinitelyNotNullType: KtDefinitelyNotNullType,
    override val parent: KSTypeReference?
) : KSDefNonNullReference {
    companion object : KSObjectCache<IdKeyPair<KtDefinitelyNotNullType, KSTypeReference?>, KSDefNonNullReferenceImpl>() {
        fun getCached(ktType: KtDefinitelyNotNullType, parent: KSTypeReference?) =
            KSDefNonNullReferenceImpl.cache
                .getOrPut(IdKeyPair(ktType, parent)) { KSDefNonNullReferenceImpl(ktType, parent) }
    }
    override val enclosedType: KSClassifierReference by lazy {
        KSClassifierReferenceImpl.getCached(ktDefinitelyNotNullType.original as KtClassType, parent)
    }
    override val typeArguments: List<KSTypeArgument>
        get() = emptyList()

    override val origin: Origin = Origin.KOTLIN

    override val location: Location
        get() = parent?.location ?: NonExistLocation
}
