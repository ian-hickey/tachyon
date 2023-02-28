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

import java.io.IOException

class TemporaryStreamHttpEntity(ts: TemporaryStream, contentType: ContentType?) : AbstractHttpEntity(), Entity4 {
    private val ts: TemporaryStream
    private val ct: ContentType?

    @get:Override
    val contentLength: Long
        get() = ts.length()

    @get:Override
    val isRepeatable: Boolean
        get() = false

    @Override
    @Throws(IOException::class)
    fun writeTo(os: OutputStream?) {
        IOUtil.copy(ts.getInputStream(), os, true, false)
    }

    @get:Throws(IOException::class, IllegalStateException::class)
    @get:Override
    val content: InputStream
        get() = ts.getInputStream()

    @get:Override
    val isStreaming: Boolean
        get() = false

    @Override
    fun contentLength(): Long {
        return contentLength
    }

    @Override
    fun contentType(): String? {
        return if (ct != null) ct.toString() else null
    }

    init {
        this.ts = ts
        ct = contentType
        setContentType(if (ct != null) ct.toString() else null)
    }
}