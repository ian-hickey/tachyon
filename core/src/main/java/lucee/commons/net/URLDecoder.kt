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
package lucee.commons.net

import java.io.UnsupportedEncodingException

object URLDecoder {
    /**
     * @param string
     * @return
     */
    fun decode(str: String, force: Boolean): String {
        return try {
            decode(str, SystemUtil.getCharset().name(), force)
        } catch (e: UnsupportedEncodingException) {
            str
        }
    }

    @Throws(UnsupportedEncodingException::class)
    fun decode(s: String, enc: String?, force: Boolean): String {
        if (!force && !ReqRspUtil.needDecoding(s)) return s
        // if(true) return java.net.URLDecoder.decode(s, enc);
        var needToChange = false
        val sb = StringBuilder()
        val numChars: Int = s.length()
        var i = 0
        while (i < numChars) {
            var c: Char = s.charAt(i)
            when (c) {
                '+' -> {
                    sb.append(' ')
                    i++
                    needToChange = true
                }
                '%' -> {
                    try {
                        val bytes = ByteArray((numChars - i) / 3)
                        var pos = 0
                        while (i + 2 < numChars && c == '%') {
                            bytes[pos++] = Integer.parseInt(s.substring(i + 1, i + 3), 16) as Byte
                            i += 3
                            if (i < numChars) c = s.charAt(i)
                        }
                        if (i < numChars && c == '%') {
                            needToChange = true
                            sb.append(c)
                            i++
                            continue
                            // throw new IOException("Incomplete trailing escape (%) pattern");
                        }
                        sb.append(String(bytes, 0, pos, enc))
                    } catch (e: NumberFormatException) {
                        needToChange = true
                        sb.append(c)
                        i++
                        // throw new IOException("Illegal hex characters in escape (%) pattern - " + e.getMessage());
                    }
                    needToChange = true
                }
                else -> {
                    sb.append(c)
                    i++
                }
            }
        }
        return if (needToChange) sb.toString() else s
    }
}