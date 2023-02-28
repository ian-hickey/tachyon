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
package lucee.runtime.functions.international

import java.lang.ref.SoftReference

/**
 * Implements the CFML Function lsparsecurrency
 */
object LSParseNumber : Function {
    private const val serialVersionUID = 2219030609677513651L
    private val formatters: Map<Locale?, SoftReference<NumberFormat?>?>? = ConcurrentHashMap<Locale?, SoftReference<NumberFormat?>?>()
    @Throws(PageException::class)
    fun call(pc: PageContext?, string: String?): Double {
        return toDoubleValue(pc.getLocale(), string)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, string: String?, locale: Locale?): Double {
        return toDoubleValue(if (locale == null) pc.getLocale() else locale, string)
    }

    @Throws(PageException::class)
    fun toDoubleValue(locale: Locale?, str: String?): Double {
        var str = str
        val tmp: SoftReference<NumberFormat?> = formatters.remove(locale)
        var nf: NumberFormat? = if (tmp == null) null else tmp.get()
        if (nf == null) {
            nf = NumberFormat.getInstance(locale)
        }
        return try {
            str = optimze(str.toCharArray())
            val pp = ParsePosition(0)
            val result: Number = nf.parse(str, pp)
            if (pp.getIndex() < str!!.length()) {
                throw ExpressionException("can't parse String [" + str + "] against locale [" + LocaleFactory.getDisplayName(locale) + "] to a number")
            }
            if (result == null) throw ExpressionException("can't parse String [" + str + "] against locale [" + LocaleFactory.getDisplayName(locale) + "] to a number")
            result.doubleValue()
        } finally {
            formatters.put(locale, SoftReference<NumberFormat?>(nf))
        }
    }

    private fun optimze(carr: CharArray?): String? {
        val sb = StringBuilder()
        var c: Char
        for (i in carr.indices) {
            c = carr!![i]
            if (!Character.isWhitespace(c) && c != '+') sb.append(carr[i])
        }
        return sb.toString()
    }
}