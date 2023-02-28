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
package tachyon.runtime.cache.util

import java.util.regex.Pattern

/**
 * Wildcard Filter
 */
class WildCardFilter(private val wildcard: String?, private val ignoreCase: Boolean) : CacheKeyFilter {
    private val pattern: Pattern?
    @Override
    fun accept(key: String?): Boolean {
        return pattern.matcher(if (ignoreCase) key.toLowerCase() else key).matches()
    }

    @Override
    override fun toString(): String {
        return "Wildcardfilter:$wildcard"
    }

    @Override
    fun toPattern(): String? {
        return wildcard
    }

    companion object {
        private val specials: String? = "{}[]().+\\^$"
    }

    /**
     * @param wildcard
     * @throws MalformedPatternException
     */
    init {
        val sb = StringBuilder(wildcard!!.length())
        val len: Int = wildcard!!.length()
        for (i in 0 until len) {
            val c: Char = wildcard.charAt(i)
            if (c == '*') sb.append(".*") else if (c == '?') sb.append('.') else if (specials.indexOf(c) !== -1) sb.append('\\').append(c) else sb.append(c)
        }
        pattern = Pattern.compile(if (ignoreCase) sb.toString().toLowerCase() else sb.toString())
        // pattern=new Perl5Compiler().compile(ignoreCase?sb.toString().toLowerCase():sb.toString());
    }
}