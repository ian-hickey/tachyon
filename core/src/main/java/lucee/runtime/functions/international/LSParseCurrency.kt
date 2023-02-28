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
/**
 * Implements the CFML Function lsparsecurrency
 */
package lucee.runtime.functions.international

import java.text.NumberFormat

object LSParseCurrency : Function {
    private const val serialVersionUID = -7023441119083818436L
    private val currFormatter: WeakHashMap? = WeakHashMap()
    private val numbFormatter: WeakHashMap? = WeakHashMap()
    @Throws(PageException::class)
    fun call(pc: PageContext?, string: String?): String? {
        return Caster.toString(toDoubleValue(pc.getLocale(), string, false))
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, string: String?, locale: Locale?): String? {
        return Caster.toString(toDoubleValue(if (locale == null) pc.getLocale() else locale, string, false))
    }

    @Throws(PageException::class)
    fun toDoubleValue(locale: Locale?, str: String?): Double {
        return toDoubleValue(locale, str, false)
    }

    @Throws(PageException::class)
    fun toDoubleValue(locale: Locale?, str: String?, strict: Boolean): Double {
        var str = str
        str = str.trim()
        val cnf: NumberFormat? = getCurrencyInstance(locale)
        val currency: Currency = cnf.getCurrency()
        if (currency.getCurrencyCode().equals("XXX")) throw ExpressionException("Unknown currency [" + locale.toString().toString() + "]")
        cnf.setParseIntegerOnly(false)
        return try {
            cnf.parse(str).doubleValue()
        } catch (e: ParseException) {
            val stripped: String = str.replace(currency.getSymbol(locale), "").replace(currency.getCurrencyCode(), "")
            val nf: NumberFormat? = getInstance(locale)
            val pp = ParsePosition(0)
            val n: Number = nf.parse(stripped, pp)
            if (n == null || pp.getIndex() === 0 || strict && stripped.length() !== pp.getIndex()) throw ExpressionException(String.format("Unparseable value [%s] for currency %s", str, locale.toString()))
            n.doubleValue()
        }
    }

    private fun getInstance(locale: Locale?): NumberFormat? {
        val o: Object = numbFormatter.get(locale)
        if (o != null) return o as NumberFormat
        val nf: NumberFormat = NumberFormat.getInstance(locale)
        numbFormatter.put(locale, nf)
        return nf
    }

    private fun getCurrencyInstance(locale: Locale?): NumberFormat? {
        val o: Object = currFormatter.get(locale)
        if (o != null) return o as NumberFormat
        val nf: NumberFormat = NumberFormat.getCurrencyInstance(locale)
        currFormatter.put(locale, nf)
        return nf
    }
}