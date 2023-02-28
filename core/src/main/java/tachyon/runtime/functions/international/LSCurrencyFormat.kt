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
 * Implements the CFML Function lscurrencyformat
 */
package tachyon.runtime.functions.international

import java.text.NumberFormat

class LSCurrencyFormat : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, args[0], Caster.toString(args[1]), Caster.toLocale(args[2]))
        if (args.size == 2) return call(pc, args[0], Caster.toString(args[1]))
        if (args.size == 1) return call(pc, args[0])
        throw FunctionException(pc, "LSCurrencyFormat", 1, 3, args.size)
    }

    companion object {
        private const val NBSP = 160.toChar()
        private const val serialVersionUID = -3173006221339130136L
        @Throws(PageException::class)
        fun call(pc: PageContext?, number: Object?): String? {
            return format(toDouble(number), "local", pc.getLocale())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, number: Object?, type: String?): String? {
            return format(toDouble(number), type, pc.getLocale())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, number: Object?, type: String?, locale: Locale?): String? {
            return format(toDouble(number), type, if (locale == null) pc.getLocale() else locale)
        }

        @Throws(ExpressionException::class)
        fun format(number: Double, type: String?, locale: Locale?): String? {
            var type = type
            if (StringUtil.isEmpty(type)) return local(locale, number)
            type = type.trim().toLowerCase()
            return if (type.equals("none")) none(locale, number) else if (type.equals("local")) local(locale, number) else if (type.equals("international")) international(locale, number) else {
                throw ExpressionException("invalid type for function lsCurrencyFormat", "types are: local, international or none")
            }
        }

        fun none(locale: Locale?, number: Double): String? {
            val nf: NumberFormat = NumberFormat.getCurrencyInstance(locale)
            return clean(StringUtil.replace(nf.format(number), nf.getCurrency().getSymbol(locale), "", false))
        }

        fun local(locale: Locale?, number: Double): String? {
            return clean(NumberFormat.getCurrencyInstance(locale).format(number))
        }

        fun international(locale: Locale?, number: Double): String? {
            val nf: NumberFormat = NumberFormat.getCurrencyInstance(locale)
            val currency: Currency = nf.getCurrency()
            val str = clean(StringUtil.replace(nf.format(number), nf.getCurrency().getSymbol(locale), "", false))
            return currency.getCurrencyCode().toString() + " " + str
        }

        private fun clean(str: String?): String? {
            // Java 10 returns nbsp instead of a regular space
            return str.replace(NBSP, ' ').trim()
        }

        @Throws(PageException::class)
        fun toDouble(number: Object?): Double {
            return if (number is String && (number as String?)!!.length() === 0) 0.0 else Caster.toDoubleValue(number) + 0.000000000001
            // adding this only influence if the binary representation is a little bit off
        }
    }
}