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
package tachyon.commons.net.http.httpclient

import java.io.FileNotFoundException

class ResourceBody(res: Resource, mimetype: String?, fileName: String?, charset: String) : AbstractContentBody(if (StringUtil.isEmpty(mimetype, true)) DEFAULT_MIMETYPE else mimetype) {
    private val fileName: String? = null
    private val res: Resource?

    @get:Override
    val charset: String

    @get:Override
    val filename: String
        get() = fileName ?: "noname"

    @Override
    @Throws(IOException::class)
    fun writeTo(os: OutputStream?) {
        IOUtil.copy(res, os, false)
    }

    @get:Override
    val contentLength: Long
        get() = if (res != null) {
            res.length()
        } else 0

    @get:Override
    val transferEncoding: String
        get() = MIME.ENC_BINARY

    /**
     * @return the res
     */
    val resource: Resource?
        get() = res

    companion object {
        const val DEFAULT_MIMETYPE = "application/octet-stream"
    }

    init {
        this.res = res
        if (!res.isFile()) {
            throw FileNotFoundException("File is not a normal file.")
        }
        if (!res.isReadable()) {
            throw FileNotFoundException("File is not readable.")
        }
        this.fileName = if (StringUtil.isEmpty(fileName, true)) res.getName() else fileName
        this.charset = charset
    }
}