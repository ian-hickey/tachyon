/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.commons.io.res.util

import java.util.ArrayList

/**
 * a WildcardPattern that accepts a comma- (or semi-colon-) separated value of patterns, e.g.
 * "*.gif, *.jpg, *.jpeg, *.png" and an optional isExclude boolean value which negates the results
 * of the default implementation
 *
 * also, lines 31 - 35 allow to set isExclude to true by passing a pattern whose first character is
 * an exclamation point '!'
 *
 * @author Igal
 */
class WildcardPattern(pattern: String, isCaseSensitive: Boolean, isExclude: Boolean, delimiters: String?) {
    private val pattern: String
    private val isInclude: Boolean
    private val patterns: List<ParsedPattern>

    /** calls this( pattern, isCaseSensitive, false, delimiters );  */
    constructor(pattern: String, isCaseSensitive: Boolean, delimiters: String?) : this(pattern, isCaseSensitive, false, delimiters) {}

    fun isMatch(input: String): Boolean {
        for (pp in patterns) {
            if (pp.isMatch(input)) return isInclude
        }
        return !isInclude
    }

    @Override
    override fun toString(): String {
        return "WildcardPattern: $pattern"
    }

    class ParsedPattern(pattern: String, isCaseSensitive: Boolean) {
        private val parts: Array<String>
        private val isCaseSensitive: Boolean

        /** calls this( pattern, false, false );  */
        constructor(pattern: String) : this(pattern, false) {}

        /** tests if the input string matches the pattern  */
        fun isMatch(input: String): Boolean {
            var input = input
            if (!isCaseSensitive) input = input.toLowerCase()
            if (parts.size == 1) return parts[0] === MATCH_ANY || parts[0].equals(input)
            if (parts.size == 2) {
                if (parts[0] === MATCH_ANY) return input.endsWith(parts[1])
                if (parts[parts.size - 1] === MATCH_ANY) return input.startsWith(parts[0])
            }
            var pos = 0
            val len: Int = input.length()
            var doMatchAny = false
            for (part in parts) {
                if (part === MATCH_ANY) {
                    doMatchAny = true
                    continue
                }
                if (part === MATCH_ONE) {
                    doMatchAny = false
                    pos++
                    continue
                }
                val ix: Int = input.indexOf(part, pos)
                if (ix == -1) return false
                if (!doMatchAny && ix != pos) return false
                pos = ix + part.length()
                doMatchAny = false
            }
            return if (parts[parts.size - 1] !== MATCH_ANY && len != pos) false else true
        }

        @Override
        override fun toString(): String {
            val sb = StringBuilder()
            for (s in parts) sb.append(s)
            return sb.toString()
        }

        companion object {
            const val MATCH_ANY = "*"
            const val MATCH_ONE = "?"
        }

        init {
            var pattern = pattern
            this.isCaseSensitive = isCaseSensitive
            if (!isCaseSensitive) pattern = pattern.toLowerCase()
            val lsp: List<String> = ArrayList<String>()
            val len: Int = pattern.length()
            var subStart = 0
            for (i in subStart until len) {
                val c: Char = pattern.charAt(i)
                if (c == '*' || c == '?') {
                    if (i > subStart) lsp.add(pattern.substring(subStart, i))
                    lsp.add(if (c == '*') MATCH_ANY else MATCH_ONE)
                    subStart = i + 1
                }
            }
            if (len > subStart) lsp.add(pattern.substring(subStart))
            parts = lsp.toArray(arrayOfNulls<String>(lsp.size()))
        }
    }

    /**
     *
     * @param pattern - the wildcard pattern, or a comma/semi-colon separated value of wildcard patterns
     * @param isCaseSensitive - if true, does a case-sensitive matching
     * @param isExclude - if true, the filter becomes an Exclude filter so that only items that do not
     * match the pattern are accepted
     */
    init {
        var pattern = pattern
        var isExclude = isExclude
        if (pattern.charAt(0) === '!') { // set isExclude to true if the first char of pattern is an exclamation point '!'
            pattern = pattern.substring(1)
            isExclude = true
        }
        this.pattern = pattern
        isInclude = !isExclude
        val tokenizer = StringTokenizer(pattern, if (!StringUtil.isEmpty(delimiters, true)) delimiters else "|")
        patterns = ArrayList<ParsedPattern>()
        while (tokenizer.hasMoreTokens()) {
            val token: String = tokenizer.nextToken().trim()
            if (!token.isEmpty()) patterns.add(ParsedPattern(token, isCaseSensitive))
        }
    }
}