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
package lucee.commons.net.http.httpclient.entity

import org.apache.http.Header

class ByteArrayHttpEntity(barr: ByteArray?, contentType: ContentType?) : ByteArrayEntity(barr), Entity4 {
    private var ct: ContentType? = null
    private val contentLength: Int
    @Override
    fun contentLength(): Long {
        return contentLength.toLong()
    }

    @Override
    fun contentType(): String? {
        return if (ct != null) ct.toString() else null
    }

    init {
        contentLength = barr?.size ?: 0
        if (ct == null) {
            val h: Header = getContentType()
            if (h != null) {
                val tmp: lucee.commons.lang.mimetype.ContentType = HTTPUtil.toContentType(h.getValue(), null)
                if (tmp != null) ct = ContentType.create(tmp.getMimeType(), tmp.getCharset())
            }
        } else ct = contentType
    }
}