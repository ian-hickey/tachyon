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
 * Implements the CFML Function paragraphformat
 */
package lucee.runtime.functions.displayFormatting

import lucee.runtime.PageContext

object ParagraphFormat : Function {
    fun call(pc: PageContext?, str: String?): String? {
        val sb = StringBuilder(str!!.length())
        val chars: CharArray = str.toCharArray()
        var flag = false
        for (i in chars.indices) {
            val c = chars[i]
            when (c) {
                '\r' -> {
                    if (i + 1 < chars.size && chars[i + 1] == '\r') flag = false
                    sb.append(' ')
                }
                '\n' -> flag = if (flag) {
                    sb.append(" <P>\r\n")
                    false
                } else {
                    sb.append(' ')
                    true
                }
                else -> {
                    sb.append(c)
                    flag = false
                }
            }
        }
        sb.append(" <P>")
        return sb.toString()
    }
}