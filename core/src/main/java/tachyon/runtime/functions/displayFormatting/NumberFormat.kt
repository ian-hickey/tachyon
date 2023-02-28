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
 * Implements the CFML Function numberformat
 */
package tachyon.runtime.functions.displayFormatting

import java.util.Locale

/**
 * Formats a Number by given pattern
 */
object NumberFormat : Function {
    /**
     * @param pc
     * @param object
     * @return formated number
     * @throws ExpressionException
     */
    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?): String? {
        return NumberFormat().format(Locale.US, toNumber(pc, `object`, 0)).replace('\'', ',')
    }

    /**
     * @param pc
     * @param object
     * @param mask
     * @return formated number
     * @throws ExpressionException
     */
    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, mask: String?): String? {
        if (mask == null) return call(pc, `object`)
        if (mask.equalsIgnoreCase("roman")) {
            return intToRoman(pc, toNumber(pc, `object`, 0).toInt())
        } else if (mask.equalsIgnoreCase("hex")) {
            return Integer.toHexString(toNumber(pc, `object`, 0).toInt())
        } else if (mask.equalsIgnoreCase(",")) {
            return call(pc, `object`)
        }
        return try {
            val _mask: Mask = tachyon.runtime.util.NumberFormat.convertMask(mask)
            NumberFormat().format(Locale.US, toNumber(pc, `object`, _mask.right), _mask)
        } catch (e: InvalidMaskException) {
            throw FunctionException(pc, "numberFormat", 2, "mask", e.getMessage())
        }
    }

    @Throws(PageException::class)
    fun toNumber(pc: PageContext?, `object`: Object?, digits: Int): Double {
        var d: Double = Caster.toDoubleValue(`object`, true, Double.NaN)
        if (Decision.isValid(d)) {
            if (digits < 12) d += 0.000000000001 // adding this only influence if the binary representation is a little bit off
            else if (digits < 15) d += 0.000000000000001 // adding this only influence if the binary representation is a little bit off
            return d
        }
        val str: String = Caster.toString(`object`)
        if (str.length() === 0) return 0
        throw FunctionException(pc, "numberFormat", 1, "number", "can't cast value [$str] to a number")
    }

    @Throws(FunctionException::class)
    private fun intToRoman(pc: PageContext?, value: Int): String? {
        var value = value
        if (value == 0) throw FunctionException(pc, "numberFormat", 1, "number", "a roman value can't be 0")
        if (value < 0) throw FunctionException(pc, "numberFormat", 1, "number", "a roman value can't be less than 0")
        if (value > 3999) throw FunctionException(pc, "numberFormat", 1, "number", "a roman value can't be greater than 3999")
        val roman = StringBuilder()
        while (value / 1000 >= 1) {
            roman.append('M')
            value = value - 1000
        }
        if (value / 900 >= 1) {
            roman.append("CM")
            value = value - 900
        }
        if (value / 500 >= 1) {
            roman.append("D")
            value = value - 500
        }
        if (value / 400 >= 1) {
            roman.append("CD")
            value = value - 400
        }
        while (value / 100 >= 1) {
            roman.append("C")
            value = value - 100
        }
        if (value / 90 >= 1) {
            roman.append("XC")
            value = value - 90
        }
        if (value / 50 >= 1) {
            roman.append("L")
            value = value - 50
        }
        if (value / 40 >= 1) {
            roman.append("XL")
            value = value - 40
        }
        while (value / 10 >= 1) {
            roman.append("X")
            value = value - 10
        }
        if (value / 9 >= 1) {
            roman.append("IX")
            value = value - 9
        }
        if (value / 5 >= 1) {
            roman.append("V")
            value = value - 5
        }
        if (value / 4 >= 1) {
            roman.append("IV")
            value = value - 4
        }
        while (value >= 1) {
            roman.append("I")
            value = value - 1
        }
        return roman.toString()
    }
}