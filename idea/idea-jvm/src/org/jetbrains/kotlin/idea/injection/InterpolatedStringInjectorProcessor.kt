/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.idea.injection

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.Trinity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.intellij.plugins.intelliLang.inject.InjectedLanguage
import org.intellij.plugins.intelliLang.inject.InjectorUtils
import org.intellij.plugins.intelliLang.inject.config.BaseInjection
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

typealias Injection = Trinity<PsiLanguageInjectionHost, InjectedLanguage, TextRange>

data class InjectionSplitResult(val isUnparsable: Boolean, val ranges: List<Injection>)

fun splitLiteralToInjectionParts(injection: BaseInjection, literal: KtStringTemplateExpression): InjectionSplitResult? {
    InjectorUtils.getLanguage(injection) ?: return null

    fun injectionRange(range: TextRange, prefix: String, suffix: String): Injection {
        TextRange.assertProperRange(range, injection)
        val injectedLanguage = InjectedLanguage.create(injection.injectedLanguageId, prefix, suffix, true)!!
        return Trinity.create(literal, injectedLanguage, range)
    }

    tailrec fun collectInjections(
        children: List<PsiElement>,
        pendingPrefix: String,
        unparseble: Boolean,
        collected: MutableList<Injection>
    ): InjectionSplitResult {
        val child = children.firstOrNull() ?: return InjectionSplitResult(unparseble, collected)
        val tail = children.subList(1, children.size)
        val partOffsetInParent = child.startOffsetInParent

        val suffix = if (tail.isEmpty()) injection.suffix else ""

        when (child) {
            is KtLiteralStringTemplateEntry, is KtEscapeStringTemplateEntry -> {
                val consequentStringsCount = tail.asSequence()
                    .takeWhile { it is KtLiteralStringTemplateEntry || it is KtEscapeStringTemplateEntry }
                    .count()

                val lastChild = children[consequentStringsCount]
                val remaining = tail.subList(consequentStringsCount, tail.size)

                collected += injectionRange(
                    TextRange.create(partOffsetInParent, lastChild.startOffsetInParent + lastChild.textLength),
                    pendingPrefix,
                    if (remaining.isEmpty()) injection.suffix else ""
                )
                return collectInjections(remaining, "", unparseble, collected)
            }

            else -> {
                if (!pendingPrefix.isEmpty()) {
                    // Store part with prefix before replacing it
                    collected += injectionRange(TextRange.from(partOffsetInParent, 0), pendingPrefix, suffix)
                }

                val (prefix, myUnparseble) = when (child) {
                    is KtSimpleNameStringTemplateEntry ->
                        tryEvaluateConstant(child.expression)?.let { it to false } ?: ((child.expression?.text ?: NO_VALUE_NAME) to true)
                    is KtBlockStringTemplateEntry ->
                        tryEvaluateConstant(child.expression)?.let { it to false } ?: (NO_VALUE_NAME to true)
                    else ->
                        ((child.text ?: NO_VALUE_NAME) to true)
                }

                if (tail.isEmpty() && !prefix.isEmpty()) {
                    // There won't be more elements, so create part with prefix right away
                    collected += injectionRange(TextRange.from(partOffsetInParent + child.textLength, 0), prefix, suffix)
                }
                return collectInjections(tail, prefix, unparseble || myUnparseble, collected)
            }
        }
    }

    val children = literal.children.toList()
    val firstChild = children.firstOrNull() ?: return null
    return if (firstChild is KtSimpleNameStringTemplateEntry || firstChild is KtBlockStringTemplateEntry) {
        val collected = ArrayList<Injection>()
        collected += injectionRange(
            TextRange.from(firstChild.startOffsetInParent, 0),
            injection.prefix,
            if (children.size == 1) injection.suffix else ""
        )
        collectInjections(children, "", false, collected)
    } else
        collectInjections(children, injection.prefix, false, ArrayList())
}


private fun tryEvaluateConstant(ktExpression: KtExpression?) =
    ktExpression?.let { expression ->
        ConstantExpressionEvaluator.getConstant(expression, expression.analyze())
            ?.takeUnless { it.isError }
            ?.getValue(TypeUtils.NO_EXPECTED_TYPE)
            ?.safeAs<String>()
    }

private const val NO_VALUE_NAME = "missingValue"
