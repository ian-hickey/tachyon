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
package lucee.runtime.net.http

import java.io.BufferedReader

class DevNullHttpServletRequest(req: HttpServletRequest?) : HttpServletRequestWrapper(req) {
    private val barr: ByteArrayInputStream?

    @get:Override
    val contentLength: Int
        get() = -1

    @get:Override
    val contentType: String?
        get() = null

    @get:Throws(IOException::class)
    @get:Override
    val inputStream: ServletInputStream?
        get() = ServletInputStreamDummy(barr)

    @get:Throws(IOException::class)
    @get:Override
    val reader: BufferedReader?
        get() = BufferedReader(InputStreamReader(barr))

    init {
        barr = ByteArrayInputStream(byteArrayOf())
    }
}