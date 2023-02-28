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
package lucee.commons.lang

import java.io.UnsupportedEncodingException

@Deprecated
@Deprecated("use instead lucee.commons.net.URLEncoder")
object URLEncoder {
    private val WWW_FORM_URL: BitSet = BitSet(256)
    @Throws(UnsupportedEncodingException::class)
    fun encode(str: String, charset: java.nio.charset.Charset?): String {
        return String(URLCodec.encodeUrl(WWW_FORM_URL, str.getBytes(charset)), "us-ascii")
    }

    @Throws(UnsupportedEncodingException::class)
    fun encode(str: String, encoding: String?): String {
        return String(URLCodec.encodeUrl(WWW_FORM_URL, str.getBytes(encoding)), "us-ascii")
    }

    @Throws(UnsupportedEncodingException::class)
    fun encode(str: String?): String {
        return encode(str, CharsetUtil.UTF8)
    }

    init {
        // alpha characters
        run {
            val i = 'a'.toInt()
            while (lucee.commons.lang.i <= 'z'.toInt()) {
                WWW_FORM_URL.set(lucee.commons.lang.i)
                lucee.commons.lang.i++
            }
        }
        run {
            val i = 'A'.toInt()
            while (lucee.commons.lang.i <= 'Z'.toInt()) {
                WWW_FORM_URL.set(lucee.commons.lang.i)
                lucee.commons.lang.i++
            }
        }
        // numeric characters
        val i = '0'.toInt()
        while (lucee.commons.lang.i <= '9'.toInt()) {
            WWW_FORM_URL.set(lucee.commons.lang.i)
            lucee.commons.lang.i++
        }
    }
}