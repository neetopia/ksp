/*
 * Copyright 2023 Google LLC
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
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

package com.google.devtools.ksp.impl.symbol.kotlin

import com.google.devtools.ksp.IdKeyPair
import com.google.devtools.ksp.KSObjectCache
import com.google.devtools.ksp.impl.symbol.kotlin.synthetic.KSTypeReferencePsiImpl
import com.google.devtools.ksp.symbol.*
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass

class KSAnnotationJavaImpl private constructor(
    private val psiAnnotation: PsiAnnotation,
    override val parent: KSNode?
): KSAnnotation {
    companion object : KSObjectCache<PsiAnnotation, KSAnnotationJavaImpl>() {
        fun getCached(psiAnnotation: PsiAnnotation, parent: KSNode?) =
            cache.getOrPut(psiAnnotation) { KSAnnotationJavaImpl(psiAnnotation ,parent) }
    }
    override val annotationType: KSTypeReference by lazy {
        KSTypeReferencePsiImpl.getCached(psiAnnotation, this)
    }
    override val arguments: List<KSValueArgument> by lazy {
        val presentValueArguments = psiAnnotation.parameterList.attributes
            .mapIndexed { index, it ->

            }
    }
    override val defaultArguments: List<KSValueArgument>
        get() = TODO("Not yet implemented")
    override val shortName: KSName
        get() = TODO("Not yet implemented")
    override val useSiteTarget: AnnotationUseSiteTarget?
        get() = TODO("Not yet implemented")
    override val origin: Origin
        get() = TODO("Not yet implemented")
    override val location: Location
        get() = TODO("Not yet implemented")

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        TODO("Not yet implemented")
    }

}
