/*******************************************************************************
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2022  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 ******************************************************************************/

package io.github.rosemoe.sora.editor.ts.predicate.builtin

import com.itsaky.androidide.treesitter.TSQuery
import com.itsaky.androidide.treesitter.TSQueryMatch
import com.itsaky.androidide.treesitter.TSQueryPredicateStep.Type
import io.github.rosemoe.sora.editor.ts.predicate.PredicateResult
import io.github.rosemoe.sora.editor.ts.predicate.TsClientPredicateStep
import io.github.rosemoe.sora.editor.ts.predicate.TsPredicate
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.PatternSyntaxException

object MatchPredicate : TsPredicate {

    private val PARAMETERS = arrayOf(Type.String, Type.Capture, Type.String, Type.Done)

    private val cache = ConcurrentHashMap<String, Regex>()

    override fun doPredicate(
        tsQuery: TSQuery,
        text: CharSequence,
        match: TSQueryMatch,
        predicate: List<TsClientPredicateStep>
    ): PredicateResult {
        if (!parametersMatch(predicate, PARAMETERS) || predicate[0].content != "match?") {
            return PredicateResult.UNHANDLED
        }
        val captured = getCaptureContent(tsQuery, match, predicate[1].content, text)
        try {
            var regex = cache[predicate[2].content]
            if (regex == null) {
                regex = Regex(predicate[2].content)
                cache[predicate[2].content] = regex
            }
            for (str in captured) {
                if (regex.find(str) == null) {
                    return PredicateResult.REJECT
                }
            }
            return PredicateResult.ACCEPT
        } catch (e: PatternSyntaxException) {
            e.printStackTrace()
            return PredicateResult.UNHANDLED
        }
    }

}