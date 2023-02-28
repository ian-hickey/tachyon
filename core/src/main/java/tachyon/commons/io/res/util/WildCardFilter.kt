/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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

import java.util.regex.Pattern

/**
 * Wildcard Filter
 */
class WildCardFilter(private val wildcard: String, ignoreCase: Boolean) : ResourceAndResourceNameFilter {
    private val pattern: Pattern
    private val ignoreCase: Boolean

    constructor(wildcard: String) : this(wildcard, IS_WIN) {}

    @Override
    fun accept(file: Resource): Boolean {
        return pattern.matcher(if (ignoreCase) StringUtil.toLowerCase(file.getName()) else file.getName()).matches()
    }

    @Override
    fun accept(parent: Resource?, name: String?): Boolean {
        return pattern.matcher(if (ignoreCase) StringUtil.toLowerCase(name) else name).matches()
    }

    fun accept(name: String?): Boolean {
        return pattern.matcher(if (ignoreCase) StringUtil.toLowerCase(name) else name).matches()
    }

    @Override
    override fun toString(): String {
        return "Wildcardfilter:$wildcard"
    } /*
	 * public static void main(String[] args) { WildCardFilter filter = new WildCardFilter("*.cfc",
	 * true); assertTrue(filter.accept("susi.cfc")); assertFalse(filter.accept("susi.cf"));
	 * assertTrue(filter.accept(".cfc")); assertTrue(filter.accept("xx.CFC"));
	 * 
	 * filter = new WildCardFilter("*.cfc", false); assertTrue(filter.accept("susi.cfc"));
	 * assertFalse(filter.accept("susi.cf")); assertTrue(filter.accept(".cfc"));
	 * assertFalse(filter.accept("xx.CFC"));
	 * 
	 * filter = new WildCardFilter("ss?xx.cfc", false); assertFalse(filter.accept("susi.cfc"));
	 * assertTrue(filter.accept("ss1xx.cfc")); assertFalse(filter.accept("ss12xx.cfc"));
	 * 
	 * 
	 * filter = new WildCardFilter("ss*xx.cfc", false); assertFalse(filter.accept("susi.cfc"));
	 * assertTrue(filter.accept("ss1xx.cfc")); assertTrue(filter.accept("ss12xx.cfc"));
	 * 
	 * filter = new WildCardFilter("ss*xx.cfc", false);
	 * assertTrue(filter.accept("ss{}[]().+\\^$ss12xx.cfc"));
	 * 
	 * print.e("done"); }
	 * 
	 * private static void assertTrue(boolean b) { if(!b) throw new
	 * RuntimeException("value is false, but true is expected"); }
	 * 
	 * private static void assertFalse(boolean b) { if(b) throw new
	 * RuntimeException("value is true, but false is expected"); }
	 */

    companion object {
        private const val specials = "{}[]().+\\^$"
        private val IS_WIN: Boolean = SystemUtil.isWindows()
    }

    /**
     * @param wildcard
     * @throws MalformedPatternException
     */
    init {
        val sb = StringBuilder(wildcard.length())
        val len: Int = wildcard.length()
        for (i in 0 until len) {
            val c: Char = wildcard.charAt(i)
            if (c == '*') sb.append(".*") else if (c == '?') sb.append('.') else if (specials.indexOf(c) !== -1) sb.append('\\').append(c) else sb.append(c)
        }
        this.ignoreCase = ignoreCase
        pattern = Pattern.compile(if (ignoreCase) StringUtil.toLowerCase(sb.toString()) else sb.toString())
    }
}