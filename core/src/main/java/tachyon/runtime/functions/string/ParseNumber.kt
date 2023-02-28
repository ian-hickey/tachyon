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
package tachyon.runtime.functions.string

import tachyon.commons.lang.StringUtil

object ParseNumber {
    private const val BIN = 2
    private const val OCT = 8
    private const val DEC = 10
    private const val HEX = 16
    @Throws(PageException::class)
    fun call(pc: PageContext?, strNumber: String?): Double {
        return call(pc, strNumber, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strNumber: String?, strRadix: String?): Double {
        return invoke(strNumber, strRadix)
    }

    operator fun invoke(strNumber: String?, strRadix: String?, defaultValue: Double): Double {
        return try {
            invoke(strNumber, strRadix)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Throws(PageException::class)
    operator fun invoke(strNumber: String?, strRadix: String?): Double {
        var strNumber = strNumber
        var strRadix = strRadix
        strNumber = strNumber.trim()
        var radix = DEC
        if (strRadix == null) {
            if (StringUtil.startsWithIgnoreCase(strNumber!!, "0x")) {
                radix = HEX
                strNumber = strNumber.substring(2)
            } else if (strNumber.startsWith("#")) {
                radix = HEX
                strNumber = strNumber.substring(1)
            } else if (strNumber.startsWith("0") && strNumber.length() > 1 && strNumber.indexOf('.') === -1) {
                radix = OCT
                strNumber = strNumber.substring(1)
            }
        } else {
            strRadix = strRadix.trim().toLowerCase()
            if (strRadix.startsWith("bin")) radix = BIN else if (strRadix.startsWith("oct")) radix = OCT else if (strRadix.startsWith("dec")) radix = DEC else if (strRadix.startsWith("hex")) {
                if (StringUtil.startsWithIgnoreCase(strNumber!!, "0x")) strNumber = strNumber.substring(2) else if (strNumber.startsWith("#")) strNumber = strNumber.substring(1)
                radix = HEX
            } else throw ExpressionException("Invalid radix definitions, valid values are [bin, oct, dec, hex]")
        }
        if (radix == OCT && strNumber.indexOf('9') !== -1) throw ExpressionException("Digit [9] is out of range for an octal number")
        if (strNumber.indexOf('.') !== -1 && radix != DEC) throw ExpressionException("The radix con only be [dec] for floating point numbers")
        return if (radix == DEC) {
            Caster.toDoubleValue(strNumber)
        } else Integer.parseInt(strNumber, radix)
    }
}