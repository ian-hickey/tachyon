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
package lucee.runtime.functions.other

import lucee.runtime.PageContext

/**
 * Implements the CFML Function xmlformat
 */
object XMLFormat : Function {
    fun call(pc: PageContext?, xmlString: String?): String? {
        val len: Int = xmlString!!.length()
        var plus = 0
        for (pos in 0 until len) {
            val chr: Char = xmlString.charAt(pos)
            when (chr) {
                '<' -> plus += 3
                '>' -> plus += 3
                '&' -> plus += 4
                '"' -> plus += 5
                '\'' -> plus += 5
            }
        }
        if (plus == 0) return xmlString
        val chars = CharArray(len + plus)
        var count = 0
        for (pos in 0 until len) {
            val chr: Char = xmlString.charAt(pos)
            when (chr) {
                '<' -> {
                    chars[count++] = '&'
                    chars[count++] = 'l'
                    chars[count++] = 't'
                    chars[count++] = ';'
                }
                '>' -> {
                    chars[count++] = '&'
                    chars[count++] = 'g'
                    chars[count++] = 't'
                    chars[count++] = ';'
                }
                '&' -> {
                    chars[count++] = '&'
                    chars[count++] = 'a'
                    chars[count++] = 'm'
                    chars[count++] = 'p'
                    chars[count++] = ';'
                }
                '"' -> {
                    chars[count++] = '&'
                    chars[count++] = 'q'
                    chars[count++] = 'u'
                    chars[count++] = 'o'
                    chars[count++] = 't'
                    chars[count++] = ';'
                }
                '\'' -> {
                    chars[count++] = '&'
                    chars[count++] = 'a'
                    chars[count++] = 'p'
                    chars[count++] = 'o'
                    chars[count++] = 's'
                    chars[count++] = ';'
                }
                else -> chars[count++] = chr
            }
        }

        // if(start<len)sb.append(xmlString.substring(start,len));
        return String(chars)
    }
}