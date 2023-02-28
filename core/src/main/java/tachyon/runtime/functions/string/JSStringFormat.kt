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
 * Implements the CFML Function jsstringformat
 */
package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

object JSStringFormat : Function {
    private const val serialVersionUID = -4188516789835855021L
    fun call(pc: PageContext?, str: String?): String? {
        return invoke(str)
    }

    operator fun invoke(str: String?): String? {
        val len: Int = str!!.length()
        val rtn = StringBuilder(len + 10)
        var c: Char
        for (i in 0 until len) {
            c = str.charAt(i)
            when (c) {
                '\\' -> rtn.append("\\\\")
                '\n' -> rtn.append("\\n")
                '\r' -> rtn.append("\\r")
                '\f' -> rtn.append("\\f")
                '\b' -> rtn.append("\\b")
                '\t' -> rtn.append("\\t")
                '"' -> rtn.append("\\\"")
                '\'' -> rtn.append("\\\'")
                else -> rtn.append(c)
            }
        }
        return rtn.toString()
    }

    fun callx(pc: PageContext?, jsString: String?): String? { // MUST ????
        val len: Int = jsString!!.length()
        var plus = 0
        for (pos in 0 until len) {
            val chr: Char = jsString.charAt(pos)
            when (chr) {
                '\\', '\n', '\r', '\f', '\b', '\t', '"', '\'' -> plus++
            }
        }
        if (plus == 0) return jsString
        val chars = CharArray(len + plus)
        var count = 0
        for (pos in 0 until len) {
            val chr: Char = jsString.charAt(pos)
            when (chr) {
                '\\' -> {
                    chars[count++] = '\\'
                    chars[count++] = '\\'
                }
                '\'' -> {
                    chars[count++] = '\\'
                    chars[count++] = '\''
                }
                '"' -> {
                    chars[count++] = '\\'
                    chars[count++] = '"'
                }
                '\n' -> {
                    chars[count++] = '\\'
                    chars[count++] = 'n'
                }
                '\r' -> {
                    chars[count++] = '\\'
                    chars[count++] = 'r'
                }
                '\f' -> {
                    chars[count++] = '\\'
                    chars[count++] = 'f'
                }
                '\b' -> {
                    chars[count++] = '\\'
                    chars[count++] = 'b'
                }
                '\t' -> {
                    chars[count++] = '\\'
                    chars[count++] = 't'
                }
                else -> chars[count++] = chr
            }
        }
        return String(chars)
    }
}