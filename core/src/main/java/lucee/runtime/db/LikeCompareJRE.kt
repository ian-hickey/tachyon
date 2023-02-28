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
package lucee.runtime.db

import java.util.Map

/**
 * Wildcard Filter
 */
internal object LikeCompareJRE {
    private const val specials = "{}[]().?+\\^$"
    private val patterns: Map = WeakHashMap()
    private val sync: Object = Object()
    @Throws(PageException::class)
    private fun createPattern(sql: SQL?, wildcard: String, escape: String?): Pattern? {
        var pattern: Pattern? = patterns.get(wildcard + escape) as Pattern
        if (pattern != null) return pattern
        // Thread-safe compilation so only one thread compiles a pattern
        synchronized(sync) {

            // Double check in the lock
            pattern = patterns.get(wildcard + escape) as Pattern
            if (pattern != null) return pattern
            var esc = 0.toChar()
            if (!StringUtil.isEmpty(escape)) {
                esc = escape.charAt(0)
                if (escape!!.length() > 1) throw DatabaseException("Invalid escape character [$escape] has been specified in a LIKE conditional", null, sql, null)
            }
            val sb = StringBuilder(wildcard.length())
            val len: Int = wildcard.length()
            // boolean isEscape=false;
            var c: Char
            var i = 0
            while (i < len) {
                c = wildcard.charAt(i)
                if (c == esc) {
                    if (i + 1 == len) throw DatabaseException("Invalid Escape Sequence. Valid sequence pairs for this escape character are: [" + esc + "%] or [" + esc + "_]", null, sql, null)
                    c = wildcard.charAt(++i)
                    if (c == '%') sb.append(c) else if (c == '_') sb.append(c) else throw DatabaseException(
                            "Invalid Escape Sequence [" + esc + "" + c + "]. Valid sequence pairs for this escape character are: [" + esc + "%] or [" + esc + "_]", null, sql, null)
                } else {
                    if (c == '%') sb.append(".*") else if (c == '_') sb.append('.') else if (specials.indexOf(c) !== -1) sb.append('\\').append(c) else sb.append(c)
                }
                i++
            }
            try {
                patterns.put(wildcard + escape, Pattern.compile(sb.toString(), Pattern.DOTALL).also { pattern = it })
            } catch (e: PatternSyntaxException) {
                throw Caster.toPageException(e)
            }
            return pattern
        }
    }

    @Throws(PageException::class)
    fun like(sql: SQL?, haystack: String?, needle: String?): Boolean {
        return like(sql, haystack, needle, null)
    }

    @Throws(PageException::class)
    fun like(sql: SQL?, haystack: String?, needle: String?, escape: String?): Boolean {
        var haystack = haystack
        haystack = StringUtil.toLowerCase(haystack)
        val p: Pattern? = createPattern(sql, StringUtil.toLowerCase(needle), if (escape == null) null else StringUtil.toLowerCase(escape))
        return p.matcher(haystack).matches()
    }
}