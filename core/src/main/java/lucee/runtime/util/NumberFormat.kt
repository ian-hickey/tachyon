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
package lucee.runtime.util

import java.math.RoundingMode

/**
 * Number formation class
 */
class NumberFormat {
    /**
     * formats a number
     *
     * @param number
     * @return formatted number as string
     */
    fun format(locale: Locale?, number: Double): String? {
        val df: DecimalFormat? = getDecimalFormat(locale)
        df.applyPattern(",0")
        df.setGroupingSize(3)
        return df.format(number)
    }

    /**
     * format a number with given mask
     *
     * @param number
     * @param mask
     * @return formatted number as string
     * @throws InvalidMaskException
     */
    @Throws(InvalidMaskException::class)
    fun formatX(locale: Locale?, number: Double, mask: String?): String? {
        return format(locale, number, convertMask(mask))
    }

    @Throws(InvalidMaskException::class)
    fun format(locale: Locale?, number: Double, mask: Mask?): String? {
        val maskLen: Int = mask!!.str!!.length()
        val df: DecimalFormat? = getDecimalFormat(locale) // (mask);
        val gs: Int = df.getGroupingSize()
        df.applyPattern(mask.str)
        df.setGroupingSize(gs)
        df.setGroupingUsed(mask.useComma)
        df.setRoundingMode(RoundingMode.HALF_UP)
        if (df.getMaximumFractionDigits() > 100) df.setMaximumFractionDigits(if (mask.right < 11) 11 else mask.right) // the if here exists because the value is acting unprecticted in
        // some cases, so we onkly do if really necessary
        val formattedNum: String = df.format(StrictMath.abs(number))
        val formattedNumBuffer = StringBuilder(formattedNum)
        if (mask.symbolsFirst) {
            val widthBefore: Int = formattedNumBuffer.length()
            applySymbolics(formattedNumBuffer, number, mask.usePlus, mask.useMinus, mask.useDollar, mask.useBrackets)
            val offset: Int = formattedNumBuffer.length() - widthBefore
            if (formattedNumBuffer.length() < maskLen + offset) {
                val padding: Int = maskLen + offset - formattedNumBuffer.length()
                applyJustification(formattedNumBuffer, mask.justification.toInt(), padding)
            }
        } else {
            val widthBefore: Int = formattedNumBuffer.length()
            val temp = StringBuilder(formattedNumBuffer.toString())
            applySymbolics(temp, number, mask.usePlus, mask.useMinus, mask.useDollar, mask.useBrackets)
            val offset: Int = temp.length() - widthBefore
            if (temp.length() < maskLen + offset) {
                val padding: Int = maskLen + offset - temp.length()
                applyJustification(formattedNumBuffer, mask.justification.toInt(), padding)
            }
            applySymbolics(formattedNumBuffer, number, mask.usePlus, mask.useMinus, mask.useDollar, mask.useBrackets)
        }
        return formattedNumBuffer.toString()
    }

    class Mask {
        var justification = RIGHT
        var useBrackets = false
        var usePlus = false
        var useMinus = false
        var useDollar = false
        var useComma = false
        var symbolsFirst = false
        var right = 0
        var str: String? = null
    }

    private fun applyJustification(_buffer: StringBuilder?, _just: Int, padding: Int) {
        if (_just == CENTER.toInt()) centerJustify(_buffer, padding) else if (_just == LEFT.toInt()) leftJustify(_buffer, padding) else rightJustify(_buffer, padding)
    }

    private fun applySymbolics(sb: StringBuilder?, no: Double, usePlus: Boolean, useMinus: Boolean, useDollar: Boolean, useBrackets: Boolean) {
        if (useBrackets && no < 0.0) {
            addSymbol(sb, '(')
            sb.append(')')
        }
        if (usePlus) addSymbol(sb, if (no < 0.0) '-' else '+')
        if (no < 0.0 && !useBrackets && !usePlus) addSymbol(sb, '-') else if (useMinus) addSymbol(sb, ' ')
        if (useDollar) addSymbol(sb, '$')
    }

    private fun centerJustify(_src: StringBuilder?, _padding: Int) {
        val padSplit = _padding / 2 + 1
        rightJustify(_src, padSplit)
        leftJustify(_src, padSplit)
    }

    private fun rightJustify(_src: StringBuilder?, _padding: Int) {
        for (x in 0 until _padding) _src.insert(0, ' ')
    }

    private fun leftJustify(_src: StringBuilder?, _padding: Int) {
        for (x in 0 until _padding) _src.append(' ')
    }

    private fun getDecimalFormat(locale: Locale?): DecimalFormat? {
        val format: java.text.NumberFormat = java.text.NumberFormat.getInstance(locale)
        return if (format is DecimalFormat) {
            format as DecimalFormat
        } else DecimalFormat()
    }

    companion object {
        private const val LEFT: Byte = 0
        private const val CENTER: Byte = 1
        private const val RIGHT: Byte = 2
        @Throws(InvalidMaskException::class)
        fun convertMask(str: String?): Mask? {
            val mask = Mask()
            var foundDecimal = false
            var foundZero = false
            var maskLen: Int = str!!.length()
            if (maskLen == 0) throw InvalidMaskException("mask can't be an empty value")
            val maskBuffer = StringBuilder(str)
            val mod: String = StringUtil.replace(str, ",", "", true)
            if (StringUtil.startsWith(mod, '_')) mask.symbolsFirst = true
            if (str.startsWith(",.")) {
                maskBuffer.replace(0, 1, ",0")
            }
            // if(maskBuffer.charAt(0) == '.')maskBuffer.insert(0, '0');
            // print.out(maskBuffer);
            var addZero = false
            var i = 0
            while (i < maskBuffer.length()) {
                var removeChar = false
                when (maskBuffer.charAt(i)) {
                    '_', '9' -> if (foundDecimal) {
                        maskBuffer.setCharAt(i, '0')
                        mask.right++
                    } else if (foundZero) maskBuffer.setCharAt(i, '0') else maskBuffer.setCharAt(i, '#') // #
                    '.' -> {
                        if (i > 0 && maskBuffer.charAt(i - 1) === '#') maskBuffer.setCharAt(i - 1, '0')
                        if (foundDecimal) removeChar = true else foundDecimal = true
                        if (i == 0) addZero = true
                    }
                    '(', ')' -> {
                        mask.useBrackets = true
                        removeChar = true
                    }
                    '+' -> {
                        mask.usePlus = true
                        removeChar = true
                    }
                    '-' -> {
                        mask.useMinus = true
                        removeChar = true
                    }
                    ',' -> {
                        mask.useComma = true
                        if (true) {
                            removeChar = true
                            maskLen++
                        }
                    }
                    'L' -> {
                        mask.justification = LEFT
                        removeChar = true
                    }
                    'C' -> {
                        mask.justification = CENTER
                        removeChar = true
                    }
                    '$' -> {
                        mask.useDollar = true
                        removeChar = true
                    }
                    '^' -> removeChar = true
                    '0' -> {
                        if (!foundDecimal) {
                            var y = 0
                            while (y < i) {
                                if (maskBuffer.charAt(y) === '#') maskBuffer.setCharAt(y, '0')
                                y++
                            }
                        }
                        foundZero = true
                    }
                    else -> throw InvalidMaskException("invalid charcter [" + maskBuffer.charAt(i).toString() + "], valid characters are ['_', '9', '.', '0', '(', ')', '+', '-', ',', 'L', 'C', '$', '^']")
                }
                if (removeChar) {
                    maskBuffer.deleteCharAt(i)
                    maskLen--
                } else {
                    i++
                }
            }
            if (addZero) addSymbol(maskBuffer, '0')
            mask.str = String(maskBuffer)
            return mask
        }

        private fun addSymbol(sb: StringBuilder?, symbol: Char) {
            var offset = 0
            while (sb.length() > offset && Character.isWhitespace(sb.charAt(offset))) {
                offset++
            }
            sb.insert(offset, symbol)
        }
    }
}