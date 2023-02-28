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

interface HTTPResponse {
    @get:Throws(IOException::class)
    val contentAsString: String?

    @Throws(IOException::class)
    fun getContentAsString(charset: String?): String?

    @get:Throws(IOException::class)
    val contentAsStream: InputStream?

    @get:Throws(IOException::class)
    val contentAsByteArray: ByteArray?
    val contentType: ContentType?
    fun getLastHeader(name: String?): Header?
    fun getLastHeaderIgnoreCase(name: String?): Header?
    val charset: String?

    @get:Throws(IOException::class)
    val contentLength: Long
    val uRL: URL?
    val statusCode: Int
    val statusText: String?
    val protocolVersion: String?
    val statusLine: String?
    val allHeaders: Array<tachyon.commons.net.http.Header?>?
}