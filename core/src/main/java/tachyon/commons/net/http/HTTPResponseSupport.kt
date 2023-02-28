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
package tachyon.commons.net.http

import java.io.IOException

abstract class HTTPResponseSupport : HTTPResponse {
    @get:Throws(IOException::class)
    @get:Override
    val contentLength: Long
        get() {
            val ct: Header = getLastHeaderIgnoreCase("Content-Length")
            if (ct != null) return Caster.toLongValue(ct.getValue(), -1)
            var `is`: InputStream? = null
            var length: Long = 0
            return try {
                `is` = getContentAsStream()
                if (`is` == null) return 0
                val buffer = ByteArray(1024)
                var len: Int
                while (`is`.read(buffer).also { len = it } != -1) {
                    length += len.toLong()
                }
                length
            } finally {
                IOUtil.close(`is`)
            }
        }

    @get:Override
    val contentType: ContentType?
        get() {
            val header: Header = getLastHeaderIgnoreCase("Content-Type") ?: return null
            val mimeCharset: Array<String> = HTTPUtil.splitMimeTypeAndCharset(header.getValue(), null) ?: return null
            val typeSub: Array<String> = HTTPUtil.splitTypeAndSubType(mimeCharset[0])
            return ContentTypeImpl(typeSub[0], typeSub[1], mimeCharset[1])
        }

    @get:Override
    val charset: String?
        get() {
            val ct: ContentType? = contentType
            var charset: String? = null
            if (ct != null) charset = ct.getCharset()
            if (!StringUtil.isEmpty(charset)) return charset
            val pc: PageContext = ThreadLocalPageContext.get()
            return if (pc != null) pc.getWebCharset().name() else "ISO-8859-1"
        }
}