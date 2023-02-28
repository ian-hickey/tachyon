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
package tachyon.runtime.net.http

import java.io.IOException

class DevNullHttpServletResponse(httpServletResponse: HttpServletResponse?) : HttpServletResponseWrapper(httpServletResponse) {
    private val httpServletResponse: HttpServletResponse?
    @Override
    fun flushBuffer() {
    }

    @get:Override
    val response: ServletResponse?
        get() = httpServletResponse

    @get:Throws(IOException::class)
    @get:Override
    val writer: PrintWriter?
        get() = PrintWriter(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM)

    @Override
    fun reset() {
    }

    @Override
    fun resetBuffer() {
    }

    @Override
    fun setBufferSize(size: Int) {
    }

    @Override
    fun setContentLength(size: Int) {
    }

    @Override
    fun setContentType(type: String?) {
    }

    @get:Throws(IOException::class)
    @get:Override
    val outputStream: ServletOutputStream?
        get() = DevNullServletOutputStream()

    /**
     * constructor of the class
     *
     * @param httpServletResponse
     */
    init {
        this.httpServletResponse = httpServletResponse
    }
}